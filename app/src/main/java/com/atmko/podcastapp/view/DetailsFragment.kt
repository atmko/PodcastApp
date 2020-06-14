package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.atmko.podcastapp.R
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.layout_error_and_loading.*
import kotlinx.android.synthetic.main.results_recycler_view.*

class DetailsFragment : Fragment() {
    private var podcastId: String? = null

    private lateinit var podcastDetails: Podcast
    private lateinit var viewModel: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: DetailsFragmentArgs by navArgs()
        podcastId = args.podcastId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        podcastId?.let { viewModel.refresh(it) }

        configureViewModel()
    }

    private fun configureViewModel() {
        viewModel.podcastDetails.observe(viewLifecycleOwner, Observer { podcastDetails ->
            resultsRecyclerView.visibility = View.VISIBLE
            this.podcastDetails = podcastDetails
            this.podcastDetails.let {
                title.text = it.title
                description.text = it.description
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                loadingScreen.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    errorScreen.visibility = View.GONE
                    resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                errorScreen.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
    }
}