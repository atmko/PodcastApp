package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.podcastapp.R
import com.atmko.podcastapp.viewmodel.SearchViewModel

class SearchParentFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
            ViewModelProviders.of(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search_parent, container, false)
        searchViewModel.text.observe(viewLifecycleOwner, Observer {
        })
        return root
    }
}
