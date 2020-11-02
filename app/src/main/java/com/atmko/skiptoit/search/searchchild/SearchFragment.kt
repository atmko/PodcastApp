package com.atmko.skiptoit.search.searchchild

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.LayoutRecyclerViewBinding
import com.atmko.skiptoit.model.GENRE_ID_KEY
import com.atmko.skiptoit.model.GENRE_NAME_KEY
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.searchparent.SearchParentFragmentDirections
import com.atmko.skiptoit.subcriptions.SubscriptionsViewModel
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SearchFragment : BaseFragment(),
    PodcastAdapter.OnPodcastItemClickListener,
    PodcastDataSource.Listener,
    SearchViewModel.Listener,
    SubscriptionsViewModel.ToggleSubscriptionListener {

    private var _binding: LayoutRecyclerViewBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var podcastAdapter: PodcastAdapter

    private var genreId: Int = 0
    private lateinit var genreName: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SearchViewModel

    private var mSavedInstanceState: Bundle? = null

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
        _binding = LayoutRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mSavedInstanceState = savedInstanceState

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

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
        viewModel.registerBoundaryCallbackListener(this)
        getMasterActivity().subscriptionsViewModel.registerToggleListener(this)
        viewModel.handleSavedState(mSavedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
        viewModel.unregisterBoundaryCallbackListener(this)
        getMasterActivity().subscriptionsViewModel.unregisterToggleListener(this)
    }

    fun configureViews() {
        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, resources.getInteger(R.integer.list_item_column_span))
            adapter = podcastAdapter
            scrollToPosition(viewModel.scrollPosition)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val scrollPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    viewModel.scrollPosition = scrollPosition
                }
            })
        }
    }

    private fun configureViewModel() {
        viewModel.genreResults.observe(viewLifecycleOwner, Observer { subscriptions ->
            binding.errorAndLoading.loadingScreen.visibility = View.GONE
            podcastAdapter.subscriptions =
                getMasterActivity().subscriptionsViewModel.subscriptionsMap
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
        if (getMasterActivity().user != null) {
            getMasterActivity().subscriptionsViewModel.toggleSubscriptionAndNotify(podcast)
        } else {
            getMasterActivity().subscriptionsViewModel.toggleLocalSubscriptionAndNotify(podcast)
        }
    }

    override fun onPageLoading() {
        binding.pageLoading.pageLoading.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoad() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_load_page), Snackbar.LENGTH_LONG).show()
    }

    override fun notifyProcessing() {

    }

    override fun onSubscriptionToggleSuccess(isSubscribed: Boolean) {
        podcastAdapter.subscriptions =
            getMasterActivity().subscriptionsViewModel.subscriptionsMap
        podcastAdapter.notifyDataSetChanged()
    }

    override fun onSubscriptionToggleFailed() {
        Snackbar.make(requireView(), getString(R.string.toggle_subscription_failed), Snackbar.LENGTH_LONG).show()
    }

    override fun onScrollPositionRestored(scrollPosition: Int) {
        binding.resultsRecyclerView.resultsRecyclerView.scrollToPosition(viewModel.scrollPosition)
    }
}