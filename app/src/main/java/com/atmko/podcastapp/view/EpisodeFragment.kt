package com.atmko.podcastapp.view

import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.FragmentEpisodeBinding
import com.atmko.podcastapp.model.EPISODE_ID_KEY
import com.atmko.podcastapp.model.Episode
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.viewmodel.EpisodeViewModel
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

private const val SHOW_MORE_KEY = "show_more"

private const val STATUS_BAR_IDENTIFIER: String = "status_bar_height"
private const val STATUS_BAR_IDENTIFIER_TYPE: String = "dimen"
private const val STATUS_BAR_IDENTIFIER_PACKAGE: String = "android"

class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var episodeId: String? = null

    private lateinit var episodeDetails: Episode
    private lateinit var player: SimpleExoPlayer
    private var viewModel: EpisodeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            episodeId = it.getString(EPISODE_ID_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, (binding.showMore.tag as Boolean))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        player.release()
    }

    companion object {
        @JvmStatic
        fun newInstance(episodeId: String) =
            EpisodeFragment().apply {
                arguments = Bundle().apply {
                    putString(EPISODE_ID_KEY, episodeId)
                }
            }
    }

    private fun configureViews() {
        configureDetailExtrasSize()

        binding.collapsedBottomSheet.visibility = View.INVISIBLE
        binding.expandedBottomSheet.visibility = View.VISIBLE

        binding.showMore.setOnClickListener {
            toggleFullOrLimitedDescription()
        }
    }

    private fun configureDetailExtrasSize() {
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val pixelHeight: Int = displayMetrics.heightPixels
        val pixelWidth: Int = displayMetrics.widthPixels

        val pixelStatusBarHeight: Int = getStatusBarHeight()

        val includeDetailsExtras: ConstraintLayout? =
            view?.findViewById(R.id.playPanelConstraintLayout)

        val navBarHeight: Int = (activity as MasterActivity).getBinding().navView.height

        //get total weightedWidth
        val weightedWidth: Int = pixelWidth

        val detailExtrasParams = FrameLayout.LayoutParams(
            weightedWidth,
            pixelHeight - pixelStatusBarHeight - navBarHeight
        )

        includeDetailsExtras?.layoutParams = detailExtrasParams
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int =
            resources.getIdentifier(
                STATUS_BAR_IDENTIFIER,
                STATUS_BAR_IDENTIFIER_TYPE, STATUS_BAR_IDENTIFIER_PACKAGE
            )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(EpisodeViewModel::class.java)
        }

        if (savedInstanceState == null) {
            episodeId?.let { viewModel?.refresh(it) }
            binding.showMore.tag = false
        } else {
            binding.showMore.tag = savedInstanceState.get(SHOW_MORE_KEY)
        }

        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
            binding.playPanel.player = player
        }
    }

    private fun configureViewModel() {
        viewModel?.episodeDetails?.observe(viewLifecycleOwner, Observer { episodeDetails ->
            this.episodeDetails = episodeDetails
            episodeDetails?.let {details ->
                //set expanded values
                binding.expandedPodcastImageView.loadNetworkImage(details.image)
                binding.expandedTitle.text = details.podcast?.title
                binding.expandedEpisodeNumber.text = details.title
                binding.title.text = details.title

                if (binding.showMore.tag as Boolean) {
                    showFullDescription()
                } else {
                    showLimitedDescription()
                }

                //set collapsed values
                binding.collapsedPodcastImageView.loadNetworkImage(details.image)
                binding.collapsedTitle.text = details.podcast?.title
                binding.collapsedEpisodeNumber.text = details.title

                context?.let {context ->
                    // Produces DataSource instances through which media data is loaded.
                    val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, getString(R.string.app_name))
                    )
                    // This is the MediaSource representing the media to be played.
                    val audioSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(details.audio))
                    // Prepare the player with the source.
                    player.prepare(audioSource)
                    player.playWhenReady = true
                }
            }
        })

        viewModel?.loading?.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel?.loadError?.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

    //todo consolidate with details show more methods
    //limit long / short description text
    private fun toggleFullOrLimitedDescription() {
        val showMoreText = binding.showMore
        if (showMoreText.tag == false) {
            showFullDescription()
            showMoreText.tag = true
        } else {
            showLimitedDescription()
            showMoreText.tag = false
        }
    }

    //todo consolidate with details show more methods
    private fun showLimitedDescription() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        descriptionText.maxLines = resources.getInteger(R.integer.max_lines_details_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.text = Html.fromHtml(episodeDetails.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(episodeDetails.description)
        }
        showMoreText.text = getString(R.string.show_more)
    }

    //todo consolidate with details show more methods
    private fun showFullDescription() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        descriptionText.maxLines = Int.MAX_VALUE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.text = Html.fromHtml(episodeDetails.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(episodeDetails.description)
        }
        showMoreText.text = getString(R.string.show_less)
    }
}
