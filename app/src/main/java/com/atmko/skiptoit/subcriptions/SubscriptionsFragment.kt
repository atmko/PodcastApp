package com.atmko.skiptoit.subcriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentSubscriptionsBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.updatecomment.SubscriptionsAdapter
import javax.inject.Inject

class SubscriptionsFragment : BaseFragment(), SubscriptionsAdapter.OnSubscriptionItemClickListener,
    SubscriptionsViewModel.Listener,
    MasterActivity.BottomSheetListener {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SubscriptionsViewModel

    @Inject
    lateinit var subscriptionsAdapter: SubscriptionsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defineViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        configureViews()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)

        getMasterActivity().registerBottomSheetListener(this)

        viewModel.checkSyncStatusAndNotify()
        configureViewModel()
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)

        getMasterActivity().unregisterBottomSheetListener(this)
    }

    private fun defineViewModel() {
        activity?.let {
            viewModel = ViewModelProvider(
                it,
                viewModelFactory
            ).get(SubscriptionsViewModel::class.java)
        }
    }

    private fun configureBottomMargin() {
        val newLayoutParams = FrameLayout.LayoutParams(binding.root.layoutParams)
        newLayoutParams.bottomMargin = getBaseFragmentBottomMargin()
        binding.root.layoutParams = newLayoutParams
    }

    private fun configureViews() {
        binding.resultsRecyclerView.resultsRecyclerView.apply {
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

    private fun configureViewModel() {
        viewModel.subscriptionsLiveData!!.observe(viewLifecycleOwner, Observer {
            viewModel.saveSubscriptionMap(it!!)
            subscriptionsAdapter.updateList(it)
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
        if (getMasterActivity().masterActivityViewModel.currentUser != null) {
            viewModel.toggleSubscriptionAndNotify(podcast)
        } else {
            viewModel.toggleLocalSubscriptionAndNotify(podcast)
        }
    }

    override fun notifyProcessing() {

    }

    override fun onSubscriptionsSyncStatusSynced() {
        binding.syncErrorLayout.visibility = View.GONE
    }

    override fun onSubscriptionsSyncStatusSyncFailed() {
        binding.syncErrorLayout.visibility = View.VISIBLE
    }

    override fun applyChange() {
        configureBottomMargin()
    }
}