package com.atmko.skiptoit.search.searchparent

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource

class SearchParentViewModel(
    private val dataSourceFactory: PodcastDataSourceFactory
) : BaseViewModel<SearchParentViewModel.Listener>() {

    interface Listener {
        fun onQueryStringRestored(queryString: String)
        fun onTabPositionRestored(tabPosition: Int)
        fun onShowGenreLayout()
        fun onHideGenreLayout()
        fun onShowManualLayout()
        fun onHideManualLayout()
        fun onShowManualSearchBar()
        fun onHideManualSearchBar()
        fun onShowKeyboard()
        fun onHideKeyboard()
    }

    companion object {
        const val QUERY_STRING_KEY = "query_string"
        const val SEARCH_MODE_KEY = "search_mode"
        const val TAB_POSITION_KEY = "tab_position"
        const val IS_SEARCH_BOX_VISIBLE_KEY = "is_search_box_visible"
        const val IS_KEYBOARD_VISIBLE_KEY = "is_keyboard_visible"
        
        const val SEARCH_MODE_GENRE = 0
        const val SEARCH_MODE_MANUAL = 1
    }

    var queryString = ""
    var tabPosition: Int = 0
    var searchMode: Int = SEARCH_MODE_GENRE
    var isSearchBoxVisible = false
    var isKeyboardVisible = false

    private val pageSize = 20
    private val setInitialLoadSizeHint = 40

    var searchResults: LiveData<PagedList<Podcast>>? = null

    //state save variables

    var podcastDataSource: QueryPodcastDataSource

    init {
        dataSourceFactory.setTypeClass(QueryPodcastDataSource::class.java)
        podcastDataSource = (dataSourceFactory.getDataSource() as QueryPodcastDataSource)
    }

    fun handleSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            queryString = savedInstanceState.getString(QUERY_STRING_KEY, "")
            tabPosition = savedInstanceState.getInt(TAB_POSITION_KEY)
            searchMode = savedInstanceState.getInt(SEARCH_MODE_KEY)
            isSearchBoxVisible = savedInstanceState.getBoolean(IS_SEARCH_BOX_VISIBLE_KEY, false)
            isKeyboardVisible = savedInstanceState.getBoolean(IS_KEYBOARD_VISIBLE_KEY)
        }

        notifyQueryStringRestored()
        notifyTabPositionRestored()

        if (isSearchBoxVisible) {
            notifyShowManualSearchBar()
        } else {
            notifyHideManualSearchBar()
        }

        if (isKeyboardVisible) {
            notifyShowKeyboard()
        } else {
            notifyHideKeyboard()
        }

        if (searchMode == SEARCH_MODE_GENRE) {
            activateGenreModeAndNotify()
        } else {
            activateManualModeAndNotify(queryString)
        }
    }

    fun saveState(outState: Bundle) {
        outState.putString(QUERY_STRING_KEY, queryString)
        outState.putInt(TAB_POSITION_KEY, tabPosition)
        outState.putInt(SEARCH_MODE_KEY, searchMode)
        outState.putBoolean(IS_SEARCH_BOX_VISIBLE_KEY, isSearchBoxVisible)
        outState.putBoolean(IS_KEYBOARD_VISIBLE_KEY, isKeyboardVisible)
    }

    fun activateGenreModeAndNotify() {
        searchMode = SEARCH_MODE_GENRE
        notifyShowGenreLayout()
        notifyHideManualLayout()
    }

    fun activateManualModeAndNotify(queryString: String) {
        if (queryString != "") {
            searchMode = SEARCH_MODE_MANUAL
            search(queryString)
            notifyShowManualLayout()
            notifyHideGenreLayout()
            notifyHideKeyboard()
        }
    }

    fun searchButtonClicked() {
        if (isSearchBoxVisible) {
            searchResults = null
            notifyHideKeyboard()
            notifyHideManualSearchBar()
            activateGenreModeAndNotify()
        } else {
            notifyShowManualSearchBar()
            notifyShowKeyboard()
        }
    }

    fun search(queryString: String) {
        this.queryString = queryString
        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(setInitialLoadSizeHint)
            .build()
        podcastDataSource.queryString = queryString
        searchResults = LivePagedListBuilder<Int, Podcast>(dataSourceFactory, config)
            .build()
    }

    fun registerBoundaryCallbackListener(listener: PodcastDataSource.Listener) {
        podcastDataSource.registerListener(listener)
    }

    fun unregisterBoundaryCallbackListener(listener: PodcastDataSource.Listener) {
        podcastDataSource.unregisterListener(listener)
    }

    private fun notifyQueryStringRestored() {
        for (listener in listeners) {
            listener.onQueryStringRestored(queryString)
        }
    }

    private fun notifyTabPositionRestored() {
        for (listener in listeners) {
            listener.onTabPositionRestored(tabPosition)
        }
    }

    private fun notifyShowGenreLayout() {
        for (listener in listeners) {
            listener.onShowGenreLayout()
        }
    }

    private fun notifyHideGenreLayout() {
        for (listener in listeners) {
            listener.onHideGenreLayout()
        }
    }

    private fun notifyShowManualLayout() {
        for (listener in listeners) {
            listener.onShowManualLayout()
        }
    }

    private fun notifyHideManualLayout() {
        for (listener in listeners) {
            listener.onHideManualLayout()
        }
    }

    private fun notifyShowManualSearchBar() {
        for (listener in listeners) {
            listener.onShowManualSearchBar()
        }
        isSearchBoxVisible = true
    }

    private fun notifyHideManualSearchBar() {
        for (listener in listeners) {
            listener.onHideManualSearchBar()
        }
        isSearchBoxVisible = false
    }

    private fun notifyShowKeyboard() {
        for (listener in listeners) {
            listener.onShowKeyboard()
        }
        isKeyboardVisible = true
    }

    private fun notifyHideKeyboard() {
        for (listener in listeners) {
            listener.onHideKeyboard()
        }
        isKeyboardVisible = false
    }
}