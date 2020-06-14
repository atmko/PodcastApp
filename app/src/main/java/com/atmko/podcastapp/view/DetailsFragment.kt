package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.atmko.podcastapp.databinding.FragmentDetailsBinding
import com.atmko.podcastapp.databinding.ResultsRecyclerViewBinding
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.viewmodel.DetailsViewModel

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var podcastId: String? = null

    private lateinit var resultsFrameLayout: ResultsRecyclerViewBinding;
    private lateinit var podcastDetails: Podcast
    private lateinit var viewModel: DetailsViewModel

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
        configureViewModel()
    }

    private fun configureViews() {
       resultsFrameLayout = binding.includeDetailsExtras.resultsFrameLayout
    }

    private fun configureViewModel() {
        viewModel.podcastDetails.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            this.podcastDetails = podcastDetails
            this.podcastDetails.let {
                binding.title.text = it.title
                binding.description.text = it.description
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

        viewModel.loadError.observe(viewLifecycleOwner, Observer {isError ->
            isError.let {
                resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE }
        })
    }
}