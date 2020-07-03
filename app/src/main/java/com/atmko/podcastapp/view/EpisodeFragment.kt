package com.atmko.podcastapp.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.FragmentEpisodeBinding
import com.atmko.podcastapp.model.*
import com.atmko.podcastapp.services.PlaybackService
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.viewmodel.EpisodeViewModel
import com.google.android.exoplayer2.ui.DefaultTimeBar

const val EPISODE_FRAGMENT_KEY = "episode_fragment"

const val SCRUBBER_ANIM_LENGTH: Long = 100
const val SCRUBBER_HIDE_LENGTH: Long = 2000

private const val IS_RESTORING_EPISODE_KEY = "is_restoring_episode"
private const val SHOW_MORE_KEY = "show_more"

private const val STATUS_BAR_IDENTIFIER: String = "status_bar_height"
private const val STATUS_BAR_IDENTIFIER_TYPE: String = "dimen"
private const val STATUS_BAR_IDENTIFIER_PACKAGE: String = "android"

class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var episodeId: String? = null
    private var isRestoringEpisode: Boolean = false

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    private var viewModel: EpisodeViewModel? = null
    private var episodeDetails: Episode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            episodeId = it.getString(EPISODE_ID_KEY)
            isRestoringEpisode = it.getBoolean(IS_RESTORING_EPISODE_KEY)
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

    //todo nullify binding in on destroy view instead of on destroy
    override fun onDestroyView() {
        super.onDestroyView()
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
        fun newInstance(episodeId: String, isRestoringEpisode: Boolean) =
            EpisodeFragment().apply {
                arguments = Bundle().apply {
                    putString(EPISODE_ID_KEY, episodeId)
                    putBoolean(IS_RESTORING_EPISODE_KEY, isRestoringEpisode)
                }
            }
    }

    private fun configureViews() {
        configureDetailExtrasSize()

        //configure time bar click guard
        val timeBar: DefaultTimeBar = binding.playPanel.findViewById(R.id.exo_progress)
        timeBar.hideScrubber()
        val timeBarOverlayButton: Button = binding.playPanel.findViewById(R.id.timeBarOverlayButton)
        timeBarOverlayButton.setOnClickListener {
            timeBarOverlayButton.visibility = View.GONE
            timeBar.showScrubber(SCRUBBER_ANIM_LENGTH)
            Handler().postDelayed({
                timeBar.hideScrubber(SCRUBBER_ANIM_LENGTH)
                timeBarOverlayButton.visibility = View.VISIBLE

            }, SCRUBBER_HIDE_LENGTH)
        }

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

        //get total weightedWidth
        val weightedWidth: Int = pixelWidth

        val detailExtrasParams = FrameLayout.LayoutParams(
            weightedWidth, pixelHeight - pixelStatusBarHeight
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
            episodeId?.let {
                if (isRestoringEpisode) {
                    viewModel?.restoreEpisode()
                } else {
                    viewModel?.refresh(it)
                }
            }
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
                details.image?.let { binding.expandedPodcastImageView.loadNetworkImage(it) }
                binding.expandedTitle.text = details.podcast?.title
                binding.expandedEpisodeTitle.text = details.title
                binding.title.text = details.title

                if (binding.showMore.tag as Boolean) {
                    showFullDescription()
                } else {
                    showLimitedDescription()
                }

                //set collapsed values
                activity?.let {
                    (activity as MasterActivity)
                        .setCollapsedSheetValues(details.image, details.podcast?.title, details.title)
                }

                context?.let {
                    mPlaybackService?.prepareMediaForPlayback(Uri.parse(episodeDetails.audio), it)

                    if (!isRestoringEpisode) {
                        mPlaybackService?.play(it)
                        val sharedPrefs = activity?.getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)
                        sharedPrefs?.let {
                            sharedPrefs.edit()
                                .putString(EPISODE_ID_KEY, details.id)
                                .putString(EPISODE_TITLE_KEY, details.title)
                                .putString(EPISODE_DESCRIPTION_KEY, details.description)
                                .putString(EPISODE_IMAGE_KEY, details.image)
                                .putString(EPISODE_AUDIO_KEY, details.audio)
                                .putLong(EPISODE_PUBLISH_DATE_KEY, details.publishDate)
                                .putInt(EPISODE_LENGTH_IN_SECONDS_KEY, details.lengthInSeconds)
                                .putString(PODCAST_TITLE_KEY, details.podcast?.title)
                                .commit()
                        }
                    }
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
