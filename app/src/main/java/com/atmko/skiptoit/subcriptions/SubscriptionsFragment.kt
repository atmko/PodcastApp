package com.atmko.skiptoit.subcriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentSubscriptionsBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.searchchild.PodcastAdapter
import javax.inject.Inject

class SubscriptionsFragment : BaseFragment(), PodcastAdapter.OnPodcastItemClickListener,
    SubscriptionsViewModel.Listener {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SubscriptionsViewModel
    @Inject
    lateinit var subscriptionsAdapter: PodcastAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        configureBottomMargin()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        defineViewModelValues()
        configureViews()
        configureDetailsViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this )
    }

    private fun configureBottomMargin() {
        val newLayoutParams = ConstraintLayout.LayoutParams(binding.root.layoutParams)
        newLayoutParams.bottomMargin = getBaseFragmentBottomMargin()
        binding.root.layoutParams = newLayoutParams
    }

    private fun defineViewModelValues() {
        activity?.let {
            viewModel = ViewModelProvider(it,
                viewModelFactory).get(SubscriptionsViewModel::class.java)
        }

        viewModel.checkSyncStatusAndNotify()
    }

    private fun configureViews() {
        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, resources.getInteger(R.integer.list_item_column_span))
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

        binding.retrySyncButton.setOnClickListener {
            viewModel.restoreSubscriptionsAndNotify()
        }
    }

    private fun configureDetailsViewModel() {
        viewModel.subscriptions.observe(viewLifecycleOwner, Observer {
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            subscriptionsAdapter.submitList(it)
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
        val action =
            SubscriptionsFragmentDirections.actionNavigationSubscriptionsToNavigationDetails(
                podcast
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        viewModel.unsubscribeAndNotify(podcast.id)
    }

    override fun notifyProcessing() {

    }

    override fun onSubscriptionsSyncStatusSynced() {
        binding.syncErrorLayout.visibility = View.GONE
        viewModel.getSubscriptions()
    }

    override fun onSubscriptionsSyncStatusSyncFailed() {
        binding.syncErrorLayout.visibility = View.VISIBLE
    }

    override fun onStatusUpdated() {

    }

    override fun onStatusUpdateFailed() {

    }
}