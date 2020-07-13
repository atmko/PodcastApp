package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.skiptoit.R
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel

class SubscriptionsFragment : Fragment() {

    private lateinit var subscriptionsViewModel: SubscriptionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        subscriptionsViewModel =
            ViewModelProviders.of(this).get(SubscriptionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_subscriptions, container, false)
        val textView: TextView = root.findViewById(R.id.subscriptionsText)
        subscriptionsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
