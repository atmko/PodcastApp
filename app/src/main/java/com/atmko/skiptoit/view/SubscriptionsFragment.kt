package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentSubscriptionsBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.view.adapters.PodcastAdapter
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel

class SubscriptionsFragment : Fragment(), PodcastAdapter.OnPodcastItemClickListener {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private var viewModel: SubscriptionsViewModel? = null
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

        configureViews()
        configureValues()
        configureDetailsViewModel()
    }

    private fun configureViews() {
        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subscriptionsAdapter
        }
    }

    private fun configureValues() {
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(SubscriptionsViewModel::class.java)
            viewModel!!.getSubscriptions()
        }
    }

    private fun configureDetailsViewModel() {
        viewModel!!.subscriptions?.observe(viewLifecycleOwner, Observer {
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            subscriptionsAdapter.updatePodcasts(it)
        })
    }

    override fun onItemClick(podcast: Podcast) {
        val action = SubscriptionsFragmentDirections
                .actionNavigationSubscriptionsToNavigationDetails(podcast)
        view?.findNavController()?.navigate(action)
    }

    override fun onSubscriptionToggle(podcast: Podcast) {
        context?.let { Toast.makeText(it, "not yet implemented", Toast.LENGTH_SHORT).show() }
    }
}
