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
import com.atmko.podcastapp.model.GENRE_ID_KEY
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.view.adapters.PodcastAdapter
import com.atmko.podcastapp.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.layout_error_and_loading.*
import kotlinx.android.synthetic.main.results_recycler_view.*

class SearchFragment: Fragment(), PodcastAdapter.OnPodcastItemClickListener {
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
        return inflater.inflate(R.layout.results_recycler_view, container, false)
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
        resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = podcastAdapter
        }
    }

    fun configureViewModel() {
        viewModel?.searchResults?.observe(viewLifecycleOwner, Observer {subscriptions ->
            resultsRecyclerView.visibility = View.VISIBLE
            subscriptions?.let { podcastAdapter.updatePodcasts(it) }
        })

        viewModel?.loading?.observe(viewLifecycleOwner, Observer {isLoading ->
            isLoading?.let {
                loadingScreen.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    errorScreen.visibility = View.GONE
                    resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel?.loadError?.observe(viewLifecycleOwner, Observer {isError ->
            isError?.let {
                isError.let { errorScreen.visibility = if (it) View.VISIBLE else View.GONE }
            }
        })
    }

    override fun onItemClick(podcast: Podcast) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }
}