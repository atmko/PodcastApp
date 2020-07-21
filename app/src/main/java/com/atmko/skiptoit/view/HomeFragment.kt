package com.atmko.skiptoit.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.atmko.skiptoit.R
import com.atmko.skiptoit.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.trendingHeading)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            val sharedPreferences: SharedPreferences = it.getSharedPreferences(
                LAUNCH_FRAGMENT_KEY,
                Context.MODE_PRIVATE
            )

            val isFirstSetup = sharedPreferences.getBoolean(IS_FIRST_SETUP_KEY, true)
            if (isFirstSetup) {
                val action = HomeFragmentDirections.actionNavigationHomeToNavigationLaunch()
                view?.findNavController()?.navigate(action)
            } else {
                (activity as MasterActivity).showBottomPanels()
            }
        }
    }
}
