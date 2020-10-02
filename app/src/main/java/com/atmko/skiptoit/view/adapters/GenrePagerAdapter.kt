package com.atmko.skiptoit.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.atmko.skiptoit.model.Genre
import com.atmko.skiptoit.search.SearchFragment

class GenrePagerAdapter(fragmentManager: FragmentManager, behavior: Int, private val genres: List<Genre>):
    FragmentStatePagerAdapter(fragmentManager, behavior) {

    override fun getItem(position: Int): Fragment {
        return SearchFragment.newInstance(genres[position].id, genres[position].name)
    }

    override fun getCount(): Int {
        return genres.size
    }
}