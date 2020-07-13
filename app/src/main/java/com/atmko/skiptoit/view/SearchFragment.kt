package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ResultsRecyclerViewBinding
import com.atmko.skiptoit.model.GENRE_ID_KEY
import com.atmko.skiptoit.model.GENRE_NAME_KEY
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.view.adapters.PodcastAdapter
import com.atmko.skiptoit.viewmodel.SearchViewModel

class SearchFragment: Fragment(), PodcastAdapter.OnPodcastItemClickListener {
    private var _binding: ResultsRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private val podcastAdapter: PodcastAdapter =
        PodcastAdapter(arrayListOf(), R.layout.item_podcast_list, this)

    private var genreId: Int? = null
    private lateinit var genreName: String
    private var viewModel: SearchViewModel? = null

    companion object {
        @JvmStatic
        fun newInstance(genreId: Int, genreName: String) = SearchFragment().apply {
            arguments = Bundle().apply {
                this.putInt(GENRE_ID_KEY, genreId)
                this.putString(GENRE_NAME_KEY, genreName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genreId = it.getInt(GENRE_ID_KEY)
            genreName = it.getString(GENRE_NAME_KEY)!!
        }
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
            viewModel = ViewModelProviders.of(this).get(genreName, SearchViewModel::class.java)
            genreId?.let { viewModel?.fetchPodcastsByGenre(it) }
        }

        configureViewModel()
    }

    fun configureViews() {
        binding.resultsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.list_item_column_span))
            adapter = podcastAdapter
        }
    }

    fun configureViewModel() {
        viewModel?.genreResults?.observe(viewLifecycleOwner, Observer {subscriptions ->
            binding.resultsRecyclerView.visibility = View.VISIBLE
            subscriptions?.let { podcastAdapter.updatePodcasts(it) }
        })

        viewModel?.genreLoading?.observe(viewLifecycleOwner, Observer {isLoading ->
            isLoading?.let {
                binding.errorAndLoading.loadingScreen.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                    binding.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel?.genreLoadError?.observe(viewLifecycleOwner, Observer {isError ->
            isError?.let {
                binding.errorAndLoading.errorScreen.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(podcast: Podcast) {
        val action =
            SearchParentFragmentDirections.actionNavigationSearchToNavigationDetails(podcast.id)
        view?.findNavController()?.navigate(action)
    }
}