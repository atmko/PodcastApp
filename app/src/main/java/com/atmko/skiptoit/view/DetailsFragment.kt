package com.atmko.skiptoit.view

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
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.util.showFullText
import com.atmko.skiptoit.util.showLimitedText
import com.atmko.skiptoit.view.adapters.EpisodeAdapter
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.DetailsViewModel
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import javax.inject.Inject

private const val SHOW_MORE_KEY = "show_more"

class DetailsFragment : BaseFragment(), EpisodeAdapter.OnEpisodeItemClickListener,
    MasterActivity.PlayerListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding
    private lateinit var podcast: Podcast

    private lateinit var podcastDetails: PodcastDetails

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: DetailsViewModel

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
        configureDetailsViewModel()
    }

    override fun onResume() {
        super.onResume()
        (activity as MasterActivity).registerPlaybackListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, showMore)
    }

    override fun onPause() {
        super.onPause()
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
            toggleFullOrLimitedDescription()
        }

        binding.toggleSubscriptionButton.setOnClickListener {
            viewModel.toggleSubscription(podcast)
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
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(DetailsViewModel::class.java)
        viewModel.loadSubscriptionStatus(podcast.id)
        viewModel.refresh(podcast.id)
        viewModel.getEpisodes(podcast.id)

        binding.toggleSubscriptionButton.isEnabled = false

        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun configureDetailsViewModel() {
        observePodcastDetails()
        observeSubscriptionStatus()
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

    private fun observePodcastDetails() {
        //todo consider using assertion !! instead of null check ?
        viewModel.podcastDetails.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
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
        })

        viewModel.episodes!!.observe(viewLifecycleOwner, Observer { episodes ->
            episodeAdapter.submitList(episodes)
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                resultsFrameLayout.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
                    resultsFrameLayout.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

    private fun observeSubscriptionStatus() {
        viewModel.isSubscribed!!.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.toggleSubscriptionButton.setText(R.string.unsubscribe)
            } else {
                binding.toggleSubscriptionButton.setText(R.string.subscribe)
            }
        })

        viewModel.processing.observe(viewLifecycleOwner, Observer { processing ->
            processing?.let {
                binding.toggleSubscriptionButton.isEnabled = !processing
            }
        })

        viewModel.processError.observe(viewLifecycleOwner, Observer { processError ->

        })
    }

    //limit long / short description text
    private fun toggleFullOrLimitedDescription() {
        if (!showMore) {
            binding.description.showFullText(podcast.description)
            binding.showMore.text = getString(R.string.show_less)
        } else {
            val maxLines = resources.getInteger(R.integer.max_lines_details_description)
            binding.description.showLimitedText(maxLines, podcast.description)
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
}