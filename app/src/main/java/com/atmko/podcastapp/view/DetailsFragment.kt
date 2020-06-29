package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.FragmentDetailsBinding
import com.atmko.podcastapp.databinding.ResultsRecyclerViewBinding
import com.atmko.podcastapp.model.Episode
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.view.adapters.EpisodeAdapter
import com.atmko.podcastapp.viewmodel.DetailsViewModel

private const val SHOW_MORE_KEY = "show_more"

class DetailsFragment : Fragment(), EpisodeAdapter.OnEpisodeItemClickListener {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var podcastId: String? = null

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding
    private lateinit var podcastDetails: Podcast
    private var viewModel: DetailsViewModel? = null
    private val episodeAdapter: EpisodeAdapter = EpisodeAdapter(arrayListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: DetailsFragmentArgs by navArgs()
        podcastId = args.podcastId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
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

        binding.upNavigationButton.setOnClickListener {
            activity?.let {
                (activity as MasterActivity).onBackPressed()
            }
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        }

        if (savedInstanceState == null) {
            podcastId?.let { viewModel?.refresh(it) }
            binding.showMore.tag = false
        } else {
            binding.showMore.tag = savedInstanceState.get(SHOW_MORE_KEY)
        }
    }

    private fun configureViewModel() {
        viewModel?.podcastDetails?.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            this.podcastDetails = podcastDetails
            this.podcastDetails.let {
                binding.title.text = it.title

                if (binding.showMore.tag as Boolean) {
                    showFullDescription()
                } else {
                    showLimitedDescription()
                }

                binding.podcastImageView.loadNetworkImage(podcastDetails.image)
                episodeAdapter.updateEpisodes(it.episodes)
            }
        })

        viewModel?.loading?.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                resultsFrameLayout.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
                    resultsFrameLayout.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel?.loadError?.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

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

    private fun showLimitedDescription() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        descriptionText.maxLines = resources.getInteger(R.integer.max_lines_details_description)
        descriptionText.text = podcastDetails.description
        showMoreText.text = getString(R.string.show_more)
    }

    private fun showFullDescription() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        descriptionText.maxLines = Int.MAX_VALUE
        descriptionText.text = podcastDetails.description
        showMoreText.text = getString(R.string.show_less)
    }

    override fun onItemClick(episode: Episode) {
        episode.id?.let { (activity as MasterActivity).loadEpisodeIntoBottomSheet(it) }
    }
}