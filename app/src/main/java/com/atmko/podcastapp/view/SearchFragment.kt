package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.ResultsRecyclerViewBinding
import com.atmko.podcastapp.model.GENRE_ID_KEY
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.view.adapters.PodcastAdapter
import com.atmko.podcastapp.viewmodel.SearchViewModel

class SearchFragment: Fragment(), PodcastAdapter.OnPodcastItemClickListener {
    private var _binding: ResultsRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private val podcastAdapter: PodcastAdapter =
        PodcastAdapter(arrayListOf(), R.layout.item_podcast_list, this)

    private var genreId: Int? = null
    private var viewModel: SearchViewModel? = null

    companion object {
        @JvmStatic
        fun newInstance(genreId: Int) = SearchFragment().apply {
            arguments = Bundle().apply {
                this.putInt(GENRE_ID_KEY, genreId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genreId = it.getInt(GENRE_ID_KEY) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = ResultsRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()

        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
            genreId?.let { viewModel?.refresh(it) }
        }

        configureViewModel()
    }

    fun configureViews() {
        binding.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = podcastAdapter
        }
    }

    fun configureViewModel() {
        viewModel?.searchResults?.observe(viewLifecycleOwner, Observer {subscriptions ->
            binding.resultsRecyclerView.visibility = View.VISIBLE
            subscriptions?.let { podcastAdapter.updatePodcasts(it) }
        })

        viewModel?.loading?.observe(viewLifecycleOwner, Observer {isLoading ->
            isLoading?.let {
                binding.errorAndLoading.loadingScreen.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                    binding.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel?.loadError?.observe(viewLifecycleOwner, Observer {isError ->
            isError?.let {
                isError.let { binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE }
            }
        })
    }

    override fun onItemClick(podcast: Podcast) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }
}