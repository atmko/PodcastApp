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
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentDetailsBinding
import com.atmko.skiptoit.databinding.ResultsRecyclerViewBinding
import com.atmko.skiptoit.episodelist.EpisodeListViewModel
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.utils.loadNetworkImage
import com.atmko.skiptoit.utils.showFullText
import com.atmko.skiptoit.utils.showLimitedText
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

private const val SHOW_MORE_KEY = "show_more"

class DetailsFragment : BaseFragment(), EpisodeAdapter.OnEpisodeItemClickListener,
    MasterActivity.PlayerListener, DetailsViewModel.Listener, BaseBoundaryCallback.Listener{

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding
    private lateinit var podcast: Podcast

    private lateinit var podcastDetails: PodcastDetails

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
        podcast = args.podcast
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
        configureValues(savedInstanceState)
        configureViewModel()
    }

    override fun onResume() {
        super.onResume()
        detailsViewModel.registerListener(this)
        episodeListViewModel.registerBoundaryCallbackListener(this)
        (activity as MasterActivity).registerPlaybackListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, showMore)
    }

    override fun onPause() {
        super.onPause()
        detailsViewModel.unregisterListener(this)
        episodeListViewModel.unregisterBoundaryCallbackListener(this)
        (activity as MasterActivity).unregisterPlaybackListener(this)
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

    private fun configureViews() {
        configureDetailExtrasSize()

        resultsFrameLayout = binding.resultsFrameLayout
        resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = episodeAdapter
        }

        binding.playButton.setOnClickListener {
            (activity as MasterActivity).togglePlayPause()
        }

        binding.showMore.setOnClickListener {
            //todo check podcast and podcastDetails is not null before execution
            toggleFullOrLimitedDescription()
        }

        binding.toggleSubscriptionButton.setOnClickListener {
            detailsViewModel.toggleSubscriptionAndNotify(podcast)
        }
    }

    private fun updatePlayButtonIcon(isPlaying: Boolean) {
        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_pause_button_sharp, null)
            } else {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_pause_button_sharp)
            }
            binding.playButton.text = getString(R.string.pause)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_play_button_sharp, null)
            } else {
                binding.playButton.icon = resources.getDrawable(R.drawable.ic_pause_button_sharp)
            }
            binding.playButton.text = getString(R.string.play)
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        detailsViewModel = ViewModelProvider(this, viewModelFactory)
            .get(DetailsViewModel::class.java)
        detailsViewModel.loadSubscriptionStatusAndNotify(podcast.id)
        detailsViewModel.getDetailsAndNotify(podcast.id)

        episodeListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(EpisodeListViewModel::class.java)
        episodeListViewModel.getEpisodes(podcast.id)

        binding.toggleSubscriptionButton.isEnabled = false

        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun configureViewModel() {
        observeEpisodes()
    }

    private fun configureDetailExtrasSize() {
        requireView().post {
            val includeDetailsExtras: ConstraintLayout? =
                view?.findViewById(R.id.details_extras)

            val detailExtrasParams = FrameLayout.LayoutParams(
                getScreenWidth(), getExtrasHeight()
            )

            includeDetailsExtras?.layoutParams = detailExtrasParams
        }
    }

    private fun observeEpisodes() {
        episodeListViewModel.episodes!!.observe(viewLifecycleOwner, Observer { episodes ->
            episodeAdapter.submitList(episodes)
        })
    }

    //limit long / short description text
    private fun toggleFullOrLimitedDescription() {
        var description = ""
        if (podcastDetails.description != null && podcastDetails.description != "") {
            description = podcastDetails.description!!
        } else if (podcast.description != null && podcast.description != "") {
            description = podcast.description!!
        }

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

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        updatePlayButtonIcon(isPlaying)
    }

    override fun onItemClick(episode: Episode) {
        (activity as MasterActivity).loadEpisodeIntoCollapsedBottomSheet(podcast.id, episode.episodeId)
    }

    override fun notifyProcessing() {
        binding.resultsFrameLayout.pageLoading.visibility = View.VISIBLE
        binding.toggleSubscriptionButton.isEnabled = false
    }

    override fun onDetailsFetched(podcastDetails: PodcastDetails) {
        resultsFrameLayout.errorAndLoading.loadingScreen.visibility = View.GONE
        resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE

        this.podcastDetails = podcastDetails
        binding.title.text = podcastDetails.title

        if (showMore) {
            binding.description.showFullText(podcastDetails.description)
            binding.showMore.text = getString(R.string.show_less)
        } else {
            val maxLines = resources.getInteger(R.integer.max_lines_details_description)
            binding.description.showLimitedText(maxLines, podcastDetails.description)
            binding.showMore.text = getString(R.string.show_more)
        }

        binding.podcastImageView.loadNetworkImage(podcastDetails.image)
    }

    override fun onDetailsFetchFailed() {
        resultsFrameLayout.errorAndLoading.loadingScreen.visibility = View.GONE
        resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(requireView(), "Failed to get details", Snackbar.LENGTH_LONG).show()
    }

    override fun onStatusUpdated(isSubscribed: Boolean) {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE

        binding.toggleSubscriptionButton.isEnabled = true
        if (isSubscribed) {
            binding.toggleSubscriptionButton.setText(R.string.unsubscribe)
        } else {
            binding.toggleSubscriptionButton.setText(R.string.subscribe)
        }
    }

    override fun onStatusUpdateFailed() {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE
        binding.toggleSubscriptionButton.isEnabled = true
        Snackbar.make(requireView(), "Failed to update subscription", Snackbar.LENGTH_LONG).show()
    }

    override fun onStatusFetched(isSubscribed: Boolean) {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE

        binding.toggleSubscriptionButton.isEnabled = true
        if (isSubscribed) {
            binding.toggleSubscriptionButton.setText(R.string.unsubscribe)
        } else {
            binding.toggleSubscriptionButton.setText(R.string.subscribe)
        }
    }

    override fun onStatusFetchFailed() {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE
        binding.toggleSubscriptionButton.isEnabled = true
        Snackbar.make(requireView(), "Failed to get subscription data", Snackbar.LENGTH_LONG).show()
    }

    override fun onPageLoading() {
        binding.resultsFrameLayout.pageLoading.visibility = View.VISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoad() {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoadFailed() {
        binding.resultsFrameLayout.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to load page", Snackbar.LENGTH_LONG).show()
    }
}