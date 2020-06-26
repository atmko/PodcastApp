package com.atmko.podcastapp.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import com.atmko.podcastapp.services.PlaybackService
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.viewmodel.EpisodeViewModel

private const val SHOW_MORE_KEY = "show_more"

private const val STATUS_BAR_IDENTIFIER: String = "status_bar_height"
private const val STATUS_BAR_IDENTIFIER_TYPE: String = "dimen"
private const val STATUS_BAR_IDENTIFIER_PACKAGE: String = "android"

class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var episodeId: String? = null

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    private var viewModel: EpisodeViewModel? = null
    private var episodeDetails: Episode? = null

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

    override fun onStart() {
        super.onStart()
        context?.let {
            Intent(context, PlaybackService::class.java).also { intent ->
                it.startService(intent)
                it.bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mIsBound) {
            context?.unbindService(playbackServiceConnection)
        }
        mIsBound = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, (binding.showMore.tag as Boolean))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mIsBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mIsBound = true
            mPlaybackService = (service as PlaybackService.PlaybackServiceBinder).getService()
            binding.playPanel.player = mPlaybackService?.player
            binding.playPanel.showController()
        }
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
    }

    private fun configureViewModel() {
        viewModel?.episodeDetails?.observe(viewLifecycleOwner, Observer { episodeDetails ->
            this.episodeDetails = episodeDetails
            episodeDetails?.let {details ->
                //set expanded values
                binding.expandedPodcastImageView.loadNetworkImage(details.image)
                binding.expandedTitle.text = details.podcast?.title
                binding.expandedEpisodeTitle.text = details.title
                binding.title.text = details.title

                if (binding.showMore.tag as Boolean) {
                    showFullDescription()
                } else {
                    showLimitedDescription()
                }

                //set collapsed values
                binding.collapsedPodcastImageView.loadNetworkImage(details.image)
                binding.collapsedTitle.text = details.podcast?.title
                binding.collapsedEpisodeTitle.text = details.title

                context?.let { mPlaybackService?.play(Uri.parse(episodeDetails.audio), it) }
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
            descriptionText.text = Html.fromHtml(episodeDetails?.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(episodeDetails?.description)
        }
        showMoreText.text = getString(R.string.show_more)
    }

    //todo consolidate with details show more methods
    private fun showFullDescription() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        descriptionText.maxLines = Int.MAX_VALUE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.text = Html.fromHtml(episodeDetails?.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(episodeDetails?.description)
        }
        showMoreText.text = getString(R.string.show_less)
    }
}
