package com.atmko.skiptoit.view

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentDetailsBinding
import com.atmko.skiptoit.databinding.ResultsRecyclerViewBinding
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.view.adapters.EpisodeAdapter
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.DetailsViewModel
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import javax.inject.Inject

private const val SHOW_MORE_KEY = "show_more"

class DetailsFragment : BaseFragment(), EpisodeAdapter.OnEpisodeItemClickListener {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding
    private lateinit var podcastDetails: Podcast

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
        podcastDetails = args.podcast
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_MORE_KEY, showMore)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun configureViews() {
        resultsFrameLayout = binding.includeDetailsExtras.resultsFrameLayout
        resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = episodeAdapter
        }

        binding.showMore.setOnClickListener {
            toggleFullOrLimitedDescription()
        }

        binding.toggleSubscriptionButton.setOnClickListener {
            viewModel.toggleSubscription(podcastDetails)
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(DetailsViewModel::class.java)
        viewModel.loadSubscriptionstatus(podcastDetails.id)
        viewModel.refresh(podcastDetails.id)

        binding.toggleSubscriptionButton.isEnabled = false

        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun configureDetailsViewModel() {
        observePodcastDetails()
        observeSubscriptionStatus()
    }

    private fun observePodcastDetails() {
        //todo consider using assertion !! instead of null check ?
        viewModel.podcastDetails.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            this.podcastDetails = podcastDetails
            this.podcastDetails.let {
                binding.title.text = it.title

                if (showMore) {
                    showFullDescription()
                } else {
                    showLimitedDescription()
                }

                binding.podcastImageView.loadNetworkImage(podcastDetails.image)
                episodeAdapter.updateEpisodes(it.episodes)
            }
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
            showFullDescription()
        } else {
            showLimitedDescription()
        }
        showMore = !showMore
    }

    private fun showLimitedDescription() {
        val descriptionText = binding.description
        descriptionText.maxLines = resources.getInteger(R.integer.max_lines_details_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.text =
                Html.fromHtml(podcastDetails.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(podcastDetails.description)
        }
        binding.showMore.text = getString(R.string.show_more)
    }

    private fun showFullDescription() {
        val descriptionText = binding.description
        descriptionText.maxLines = Int.MAX_VALUE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.text =
                Html.fromHtml(podcastDetails.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            descriptionText.text = Html.fromHtml(podcastDetails.description)
        }
        binding.showMore.text = getString(R.string.show_less)
    }

    override fun onItemClick(episode: Episode) {
        episode.id?.let {
            (activity as MasterActivity).loadEpisodeIntoBottomSheet(podcastDetails.id, it)
        }
    }
}