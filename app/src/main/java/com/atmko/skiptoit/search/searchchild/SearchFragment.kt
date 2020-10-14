package com.atmko.skiptoit.search.searchchild

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ResultsRecyclerViewBinding
import com.atmko.skiptoit.model.GENRE_ID_KEY
import com.atmko.skiptoit.model.GENRE_NAME_KEY
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.searchparent.SearchParentFragment
import com.atmko.skiptoit.search.searchparent.SearchParentFragmentDirections
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SearchFragment : BaseFragment(),
    PodcastAdapter.OnPodcastItemClickListener,
    PodcastDataSource.Listener {

    private var _binding: ResultsRecyclerViewBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var podcastAdapter: PodcastAdapter

    private var genreId: Int = 0
    private lateinit var genreName: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SearchViewModel

    companion object {
        @JvmStatic
        fun newInstance(genreId: Int, genreName: String) = SearchFragment()
            .apply {
            arguments = Bundle().apply {
                this.putInt(GENRE_ID_KEY, genreId)
                this.putString(GENRE_NAME_KEY, genreName)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genreId = it.getInt(GENRE_ID_KEY)
            genreName = it.getString(GENRE_NAME_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ResultsRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            viewModel = ViewModelProvider(
                it,
                viewModelFactory
            ).get(genreName, SearchViewModel::class.java)
        }

        viewModel.fetchPodcastsByGenre(genreId)

        configureViews()
        configureViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.registerBoundaryCallbackListener(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterBoundaryCallbackListener(this)
    }

    fun configureViews() {
        binding.resultsRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, resources.getInteger(R.integer.list_item_column_span))
            adapter = podcastAdapter
            if (isSavedTab()) {
                scrollToPosition(getSavedScrollPosition())
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val scrollPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    saveScrollPosition(scrollPosition)
                }
            })
        }
    }

    fun saveScrollPosition(scrollPosition: Int) {
        viewModel.scrollPosition = scrollPosition
    }

    private fun getSavedScrollPosition(): Int {
        return viewModel.scrollPosition
    }

    private fun isSavedTab(): Boolean {
        val savedTabPosition = (parentFragment as SearchParentFragment).getSavedTabPosition()
        val savedTabGenreId = resources.getIntArray(R.array.genre_ids)[savedTabPosition]
        return savedTabGenreId == genreId
    }

    private fun configureViewModel() {
        viewModel.genreResults.observe(viewLifecycleOwner, Observer { subscriptions ->
            binding.errorAndLoading.loadingScreen.visibility = View.GONE
            subscriptions?.let { podcastAdapter.submitList(it) }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(podcast: Podcast) {
        val action =
            SearchParentFragmentDirections.actionNavigationSearchToNavigationDetails(
                podcast
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        //todo
        context?.let { Toast.makeText(it, "not yet implemented", Toast.LENGTH_SHORT).show() }
    }

    override fun onPageLoading() {
        binding.pageLoading.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoad() {
        binding.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to load page", Snackbar.LENGTH_LONG).show()
    }
}