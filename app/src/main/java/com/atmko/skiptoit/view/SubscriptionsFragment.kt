package com.atmko.skiptoit.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentSubscriptionsBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.view.adapters.PodcastAdapter
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel

class SubscriptionsFragment : Fragment(), PodcastAdapter.OnPodcastItemClickListener {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionsViewModel
    private val subscriptionsAdapter: PodcastAdapter =
        PodcastAdapter(arrayListOf(), R.layout.item_podcast_list, this)

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (isFirstSetup()) {
            openLaunchFragment()
            return
        } else {
            (activity as MasterActivity).showBottomPanels()
        }
        defineViewModelValues()
        configureViews()
        configureDetailsViewModel()
    }

    private fun isFirstSetup(): Boolean {
        activity?.let {
            val sharedPreferences: SharedPreferences = it.getSharedPreferences(
                LAUNCH_FRAGMENT_KEY,
                Context.MODE_PRIVATE
            )

            return sharedPreferences.getBoolean(IS_FIRST_SETUP_KEY, true)
        }

        return true
    }

    private fun openLaunchFragment() {
        val action = SubscriptionsFragmentDirections.actionNavigationSubscriptionsToNavigationLaunch()
        view?.findNavController()?.navigate(action)
    }

    private fun defineViewModelValues() {
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(SubscriptionsViewModel::class.java)
        }

        viewModel.getSubscriptions()
    }

    private fun configureViews() {
        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subscriptionsAdapter
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

    private fun configureDetailsViewModel() {
        viewModel.subscriptions.observe(viewLifecycleOwner, Observer {
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            subscriptionsAdapter.updatePodcasts(it)
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.resultsFrameLayout.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onItemClick(podcast: Podcast) {
        val action = SubscriptionsFragmentDirections
            .actionNavigationSubscriptionsToNavigationDetails(podcast)
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        viewModel.unsubscribe(podcast.id)
    }
}