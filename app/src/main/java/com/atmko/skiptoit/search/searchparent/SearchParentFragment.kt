package com.atmko.skiptoit.search.searchparent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentSearchParentBinding
import com.atmko.skiptoit.model.Genre
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.searchchild.PodcastAdapter
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

const val IS_SEARCH_BOX_VISIBLE_KEY = "is_search_box_visible"
const val IS_KEYBOARD_VISIBLE_KEY = "is_keyboard_visible"

class SearchParentFragment : BaseFragment(),
    SearchParentViewModel.Listener,
    PodcastAdapter.OnPodcastItemClickListener,
    PodcastDataSource.Listener {

    private var _binding: FragmentSearchParentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var podcastAdapter: PodcastAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SearchParentViewModel

    private var isKeyboardVisible = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchParentBinding.inflate(inflater, container, false)
        configureBottomMargin()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.toolbar.titleTextView.text = navController.currentDestination!!.label
        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
        viewModel.registerBoundaryCallbackListener(this)
        viewModel.restoreSearchModeAndNotify()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(IS_SEARCH_BOX_VISIBLE_KEY, isSearchBarShown())
        outState.putBoolean(IS_KEYBOARD_VISIBLE_KEY, isKeyboardVisible)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
        viewModel.unregisterBoundaryCallbackListener(this)
    }

    private fun configureBottomMargin() {
        val newLayoutParams = ConstraintLayout.LayoutParams(binding.root.layoutParams)
        newLayoutParams.bottomMargin = getBaseFragmentBottomMargin()
        binding.root.layoutParams = newLayoutParams
    }

    fun configureViews() {
        //configure tabs and view pager
        binding.tabLayout.removeAllTabs()
        val genresNames: Array<String> = resources.getStringArray(R.array.genre_titles)
        val genresIds: IntArray = resources.getIntArray(R.array.genre_ids)
        val genres: MutableList<Genre> = mutableListOf()
        for (i in genresNames.indices) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(genresNames[i]))
            genres.add(Genre(genresIds[i], genresNames[i]))
        }

        binding.searchViewPager.apply {
            offscreenPageLimit = 2
            adapter = GenrePagerAdapter(
                childFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, genres
            )
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        }

        binding.tabLayout.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.searchViewPager.currentItem = tab.position
                    saveTabPosition(tab.position)
                }
            })
        }

        //configure search box
        binding.toolbar.searchBox.searchBox.apply {
            setOnEditorActionListener { view, actionId, event ->
                val queryString: String = view.text.toString()
                if (queryString != "") {
                    (activity as MasterActivity).hideSoftKeyboard(this)
                    viewModel.activateManualModeAndNotify(queryString)
                }

                true
            }
        }

        binding.toolbar.searchImageButton.apply {
            setOnClickListener {
                if (isSearchBarShown()) {
                    hideKeyboard()
                    clearManualSearchData()
                    hideManualSearchState()
                    showPresetSearchState()
                } else {
                    showManualSearchBar()
                    showKeyboard()
                }
            }
        }

        //configure recycler view
        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = podcastAdapter
        }
    }

    private fun showKeyboard() {
        (activity as MasterActivity).showSoftKeyboard(binding.toolbar.searchBox.searchBox)
        isKeyboardVisible = true
    }

    private fun hideKeyboard() {
        (activity as MasterActivity).hideSoftKeyboard(binding.toolbar.searchBox.searchBox)
        isKeyboardVisible = false
    }

    private fun isSearchBarShown(): Boolean {
        return binding.toolbar.searchBox.searchBox.visibility == View.VISIBLE
    }

    private fun clearManualSearchData() {
        binding.toolbar.searchBox.searchBox.text = "".toEditable()
        podcastAdapter.submitList(null)
    }

    private fun showPresetSearchState() {
        binding.toolbar.titleTextView.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
        binding.searchViewPager.visibility = View.VISIBLE
        binding.presetSearchDivider.visibility = View.VISIBLE
    }

    private fun hideManualSearchState() {
        binding.pageLoading.pageLoading.visibility = View.GONE
        binding.resultsRecyclerView.resultsRecyclerView.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        binding.errorAndLoading.loadingScreen.visibility = View.GONE

        binding.toolbar.searchBox.searchBox.visibility = View.GONE
        binding.toolbar.searchImageButton.setImageResource(R.drawable.ic_manual_search)
    }

    private fun showManualSearchBar() {
        binding.toolbar.searchImageButton.setImageResource(R.drawable.ic_cancel_search)
        binding.toolbar.searchBox.searchBox.visibility = View.VISIBLE
        binding.toolbar.titleTextView.visibility = View.GONE
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        activity?.let {
            viewModel = ViewModelProvider(
                it,
                viewModelFactory
            ).get(SearchParentViewModel::class.java)
        }
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(viewModel.tabPosition))

        if (savedInstanceState != null) {
            val isSearchBoxVisible = savedInstanceState.getBoolean(IS_SEARCH_BOX_VISIBLE_KEY)
            if (isSearchBoxVisible) {
                showManualSearchBar()
            } else {
                clearManualSearchData()
                hideManualSearchState()
                showPresetSearchState()
            }

            val isKeyboardVisible = savedInstanceState.getBoolean(IS_KEYBOARD_VISIBLE_KEY)
            if (isKeyboardVisible) {
                showKeyboard()
            } else {
                hideKeyboard()
            }
        }
    }

    private fun configureViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { subscriptions ->
            binding.errorAndLoading.loadingScreen.visibility = View.GONE
            subscriptions?.let { podcastAdapter.submitList(it) }
        })
    }

    fun getSavedTabPosition(): Int {
        return viewModel.tabPosition
    }

    fun saveTabPosition(tabPosition: Int) {
        viewModel.tabPosition = tabPosition
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showManualSearchMode() {
        binding.tabLayout.visibility = View.GONE
        binding.searchViewPager.visibility = View.GONE
        binding.presetSearchDivider.visibility = View.GONE

        binding.pageLoading.pageLoading.visibility = View.VISIBLE
        binding.resultsRecyclerView.resultsRecyclerView.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
    }

    override fun onItemClick(podcast: Podcast) {
        val action =
            SearchParentFragmentDirections.actionNavigationSearchToNavigationDetails(
                podcast
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        context?.let { Toast.makeText(it, "not yet implemented", Toast.LENGTH_SHORT).show() }
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
        Snackbar.make(requireView(), "Failed to load page", Snackbar.LENGTH_LONG).show()
    }

    override fun onSearchModeManualActivated(queryString: String) {
        showManualSearchMode()
        viewModel.search(queryString)
        configureViewModel()
    }

    override fun onSearchModeManualRestored() {
        showManualSearchMode()
        configureViewModel()
    }

    override fun onSearchModeGenreActivated() {
        binding.tabLayout.visibility = View.VISIBLE
        binding.searchViewPager.visibility = View.VISIBLE
        binding.presetSearchDivider.visibility = View.VISIBLE

        binding.pageLoading.pageLoading.visibility = View.GONE
        binding.resultsRecyclerView.resultsRecyclerView.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
    }
}