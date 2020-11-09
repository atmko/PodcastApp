package com.atmko.skiptoit.details

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentDetailsBinding
import com.atmko.skiptoit.episodelist.EpisodeListViewModel
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.subcriptions.SubscriptionsViewModel
import com.atmko.skiptoit.utils.loadNetworkImage
import com.atmko.skiptoit.utils.showFullText
import com.atmko.skiptoit.utils.showLimitedText
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

private const val SHOW_MORE_KEY = "show_more"

class DetailsFragment : BaseFragment(), EpisodeAdapter.OnEpisodeItemClickListener,
    MasterActivity.PlayerListener, DetailsViewModel.Listener,
    SubscriptionsViewModel.ToggleSubscriptionListener,
    SubscriptionsViewModel.FetchSubscriptionStatusListener, BaseBoundaryCallback.Listener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    lateinit var podcast: Podcast

    private var podcastDetails: PodcastDetails? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var episodeListViewModel: EpisodeListViewModel

    @Inject
    lateinit var episodeAdapter: EpisodeAdapter

    private var showMore: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: DetailsFragmentArgs by navArgs()
        //todo replace with database call in view model
        podcast = args.podcast
        defineViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        configureBottomMargin()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureViewValues(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        detailsViewModel.registerListener(this)
        episodeListViewModel.registerBoundaryCallbackListener(this)
        getMasterActivity().subscriptionsViewModel.registerToggleListener(this)
        getMasterActivity().subscriptionsViewModel.registerSubscriptionStatusListener(this)

        detailsViewModel.detectOldOrNewPodcastAndNotify(podcast.id)
        detailsViewModel.getDetailsAndNotify(podcast.id)
        getMasterActivity().subscriptionsViewModel.getSubscriptionStatusAndNotify(podcast.id)

        episodeListViewModel.getEpisodes(podcast.id)
        configureViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, showMore)
    }

    override fun onStop() {
        super.onStop()
        detailsViewModel.unregisterListener(this)
        episodeListViewModel.unregisterBoundaryCallbackListener(this)
        getMasterActivity().unregisterPlaybackListener(this)
        getMasterActivity().subscriptionsViewModel.unregisterToggleListener(this)
        getMasterActivity().subscriptionsViewModel.unregisterSubscriptionStatusListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun configureBottomMargin() {
        val newLayoutParams = ConstraintLayout.LayoutParams(binding.root.layoutParams)
        newLayoutParams.bottomMargin = getBaseFragmentBottomMargin()
        binding.root.layoutParams = newLayoutParams
    }

    private fun defineViewModel() {
        detailsViewModel = ViewModelProvider(this, viewModelFactory)
            .get(DetailsViewModel::class.java)
        episodeListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(EpisodeListViewModel::class.java)
    }

    private fun configureViews() {
        configureDetailExtrasSize()

        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = episodeAdapter
        }

        binding.playButton.setOnClickListener {
            detailsViewModel.togglePlaybackAndNotify(podcast.id)
        }

        binding.showMore.setOnClickListener {
            //todo check podcast and podcastDetails is not null before execution
            setDescription(true)
        }

        binding.toggleSubscriptionButton.setOnClickListener {
            if (getMasterActivity().user != null) {
                getMasterActivity().subscriptionsViewModel.toggleSubscriptionAndNotify(podcast)
            } else {
                getMasterActivity().subscriptionsViewModel.toggleLocalSubscriptionAndNotify(podcast)
            }
        }
    }

    private fun updatePlayButtonIcon(isPlaying: Boolean) {
        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.playButton.icon =
                    resources.getDrawable(R.drawable.ic_pause_button_sharp, null)
            } else {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_pause_button_sharp)
            }
            binding.playButton.text = getString(R.string.pause)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.playButton.icon =
                    resources.getDrawable(R.drawable.ic_play_button_sharp, null)
            } else {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_pause_button_sharp)
            }
            binding.playButton.text = getString(R.string.play)
        }
    }

    private fun configureViewValues(savedInstanceState: Bundle?) {
        binding.toggleSubscriptionButton.isEnabled = false

        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun configureViewModel() {
        episodeListViewModel.episodes!!.observe(viewLifecycleOwner, Observer { episodes ->
            episodeAdapter.submitList(episodes)
        })
    }

    private fun configureDetailExtrasSize() {
        requireView().post {
            val includeDetailsExtras: ConstraintLayout? = binding.detailsExtras

            val detailExtrasParams = FrameLayout.LayoutParams(
                getScreenWidth(), getExtrasHeight()
            )

            includeDetailsExtras?.layoutParams = detailExtrasParams
        }
    }

    // todo: set description via view model
    private fun setDescription(isToggle: Boolean) {
        if (isToggle) {
            toggleFullOrLimitedDescription()
        } else {
            resetDescription()
        }
    }

    //limit long / short description text
    private fun toggleFullOrLimitedDescription() {
        val description = getDescription()
        if (!showMore) {
            binding.description.showFullText(description)
            binding.showMore.text = getString(R.string.show_less)
        } else {
            val maxLines = resources.getInteger(R.integer.max_lines_details_description)
            binding.description.showLimitedText(maxLines, description)
            binding.showMore.text = getString(R.string.show_more)
        }
        showMore = !showMore
    }

    private fun getDescription(): String {
        var description = ""
        if (podcastDetails != null && podcastDetails!!.description != null && podcastDetails!!.description != "") {
            description = podcastDetails!!.description!!
        } else if (podcast.description != null && podcast.description != "") {
            description = podcast.description!!
        }
        return description
    }

    private fun resetDescription() {
        val description = getDescription()
        if (showMore) {
            binding.description.showFullText(description)
            binding.showMore.text = getString(R.string.show_less)
        } else {
            val maxLines = resources.getInteger(R.integer.max_lines_details_description)
            binding.description.showLimitedText(maxLines, description)
            binding.showMore.text = getString(R.string.show_more)
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        updatePlayButtonIcon(isPlaying)
    }

    override fun onItemClick(episode: Episode) {
        getMasterActivity().loadEpisodeIntoCollapsedBottomSheet(
            podcast.id,
            episode.episodeId
        )
    }

    override fun notifyProcessing() {
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.toggleSubscriptionButton.isEnabled = false
    }

    override fun onSubscriptionToggleSuccess(isSubscribed: Boolean) {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE

        binding.toggleSubscriptionButton.isEnabled = true
        if (isSubscribed) {
            binding.toggleSubscriptionButton.setText(R.string.unsubscribe)
        } else {
            binding.toggleSubscriptionButton.setText(R.string.subscribe)
        }
    }

    override fun onSubscriptionToggleFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.toggleSubscriptionButton.isEnabled = true
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_update_subscription),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onSubscriptionStatusFetched(isSubscribed: Boolean) {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE

        binding.toggleSubscriptionButton.isEnabled = true
        if (isSubscribed) {
            binding.toggleSubscriptionButton.setText(R.string.unsubscribe)
        } else {
            binding.toggleSubscriptionButton.setText(R.string.subscribe)
        }
    }

    override fun onSubscriptionStatusFetchFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.toggleSubscriptionButton.isEnabled = true
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_get_subscription_data),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDetailsFetched(podcastDetails: PodcastDetails) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE

        this.podcastDetails = podcastDetails

        binding.title.text = podcastDetails.title
        setDescription(false)
        binding.podcastImageView.loadNetworkImage(podcastDetails.image)
    }

    override fun onDetailsFetchFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_get_details),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onOldPodcastDetced() {
        getMasterActivity().registerPlaybackListener(this)
    }

    override fun onNewPodcastDetcted() {

    }

    override fun onPodcastDetectFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_detect_podcast),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onToggleOldEpisodePlayback() {
        getMasterActivity().togglePlayPause()
    }

    override fun onToggleOldEpisodePlaybackFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_toggle_old_playback),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onToggleNewEpisodePlayback(latestEpisodeId: String) {
        getMasterActivity().registerPlaybackListener(this)
        getMasterActivity().loadEpisodeIntoBottomSheet(
            podcast.id,
            latestEpisodeId
        )
    }

    override fun onToggleNewEpisodePlaybackFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_toggle_new_playback),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onPageLoading() {
        binding.pageLoading.pageLoading.visibility = View.VISIBLE
    }

    override fun onPageLoad() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_load_page), Snackbar.LENGTH_LONG)
            .show()
    }
}