package com.atmko.podcastapp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.atmko.podcastapp.model.Genre
import com.atmko.podcastapp.view.SearchFragment

class GenrePagerAdapter(fragmentManager: FragmentManager, behavior: Int, private val genres: List<Genre>):
    FragmentStatePagerAdapter(fragmentManager, behavior) {

    override fun getItem(position: Int): Fragment {
        return SearchFragment.newInstance(genres[position].id)
    }

    override fun getCount(): Int {
        return genres.size
    }
}