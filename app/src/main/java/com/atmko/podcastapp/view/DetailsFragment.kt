package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class DetailsFragment : Fragment(), EpisodeAdapter.OnEpisodeItemClickListener {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var podcastId: String? = null

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding;
    private lateinit var podcastDetails: Podcast
    private lateinit var viewModel: DetailsViewModel
    private val episodeAdapter: EpisodeAdapter = EpisodeAdapter(arrayListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: DetailsFragmentArgs by navArgs()
        podcastId = args.podcastId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        podcastId?.let { viewModel.refresh(it) }

        configureViews()
        configureValues()
        configureViewModel()
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
            limitDescriptionText()
        }

        binding.upNavigationButton.setOnClickListener {
            activity?.let {
                (activity as MasterActivity).onBackPressed()
            }
        }
    }

    private fun configureValues() {
        binding.showMore.tag = false
    }

    private fun configureViewModel() {
        viewModel.podcastDetails.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            this.podcastDetails = podcastDetails
            this.podcastDetails.let {
                binding.title.text = it.title
                limitDescriptionText()
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

    //limit long text
    private fun limitDescriptionText() {
        val showMoreText = binding.showMore
        val descriptionText = binding.description
        if (showMoreText.tag == false) {
            descriptionText.maxLines = resources.getInteger(R.integer.max_lines_details_description)
            descriptionText.text = podcastDetails.description
            showMoreText.text = getString(R.string.show_more)
            showMoreText.tag = true
        } else {
            descriptionText.maxLines = Int.MAX_VALUE
            descriptionText.text = podcastDetails.description
            showMoreText.text = getString(R.string.show_less)
            showMoreText.tag = false
        }
    }

    override fun onItemClick(episode: Episode) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
        //TODO not yet implemented
    }
}