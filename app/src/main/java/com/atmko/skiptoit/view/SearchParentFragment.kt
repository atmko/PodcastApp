package com.atmko.skiptoit.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentSearchParentBinding
import com.atmko.skiptoit.model.Genre
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.util.toEditable
import com.atmko.skiptoit.view.adapters.GenrePagerAdapter
import com.atmko.skiptoit.view.adapters.PodcastAdapter
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.SearchViewModel
import com.atmko.skiptoit.viewmodel.ViewModelFactory
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class SearchParentFragment : BaseFragment(), PodcastAdapter.OnPodcastItemClickListener {
    private var _binding: FragmentSearchParentBinding? = null
    private val binding get() = _binding!!

    private val podcastAdapter: PodcastAdapter =
        PodcastAdapter(arrayListOf(), R.layout.item_podcast_list, this)

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SearchViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
        configureViewModel()
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
            adapter = GenrePagerAdapter(childFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, genres)
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
        binding.toolbar.searchBox.apply {
            setOnEditorActionListener { view, actionId, event ->
                val queryString : String = view.text.toString()
                if (queryString != "") {
                    binding.tabLayout.visibility = View.GONE
                    binding.searchViewPager.visibility = View.GONE
                    binding.presetSearchDivider.visibility = View.GONE

                    binding.resultsFrameLayout.resultsFrameLayout.visibility = View.VISIBLE
                    binding.manualSearchDivider.visibility = View.VISIBLE

                    viewModel.search(queryString)
                }

                true
            }
        }

        binding.toolbar.cancelSearchButton.apply {
            setOnClickListener {
                binding.toolbar.searchBox.text = "".toEditable()

                binding.tabLayout.visibility = View.VISIBLE
                binding.searchViewPager.visibility = View.VISIBLE
                binding.presetSearchDivider.visibility = View.VISIBLE

                binding.resultsFrameLayout.resultsFrameLayout.visibility = View.GONE
                binding.manualSearchDivider.visibility = View.GONE

                podcastAdapter.podcasts.clear()
            }
        }

        //configure recycler view
        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = podcastAdapter
        }
    }

    private fun configureValues() {
        activity?.let {
            viewModel = ViewModelProviders.of(it,
                viewModelFactory).get(SearchViewModel::class.java)
        }
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(viewModel.tabPosition))
    }

    fun configureViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { subscriptions ->
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            subscriptions?.let { podcastAdapter.updatePodcasts(it) }
        })

        viewModel.searchLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.resultsFrameLayout.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
                    binding.resultsFrameLayout.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel.searchLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE }
        })
    }

    fun getSavedTabPosition(): Int {
        return viewModel.tabPosition
    }

    fun saveTabPosition(tabPosition: Int) {
        viewModel.tabPosition = tabPosition
    }

    fun saveScrollPosition(scrollPosition: Int) {
        viewModel.scrollPosition = scrollPosition
    }

    fun getSavedScrollPosition(): Int {
        return viewModel.scrollPosition
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(podcast: Podcast) {
        val action =
            SearchParentFragmentDirections.actionNavigationSearchToNavigationDetails(podcast)
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        context?.let { Toast.makeText(it, "not yet implemented", Toast.LENGTH_SHORT).show() }
    }
}