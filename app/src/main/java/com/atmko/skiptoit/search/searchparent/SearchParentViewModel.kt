package com.atmko.skiptoit.search.searchparent

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.search.common.PodcastDataSource

class SearchParentViewModel(
    private val dataSourceFactory: PodcastDataSourceFactory
) : BaseViewModel<SearchParentViewModel.Listener>() {

    interface Listener {
        fun onSearchModeManualActivated(queryString: String)
        fun onSearchModeManualRestored()
        fun onSearchModeGenreActivated()
    }

    companion object {
        const val SEARCH_MODE_GENRE = 0
        const val SEARCH_MODE_MANUAL = 1
    }

    var searchMode: Int = SEARCH_MODE_GENRE

    private val pageSize = 20
    private val setInitialLoadSizeHint = 40

    lateinit var searchResults: LiveData<PagedList<Podcast>>

    //state save variables
    var tabPosition: Int = 0

    var podcastDataSource: QueryPodcastDataSource

    init {
        dataSourceFactory.setTypeClass(QueryPodcastDataSource::class.java)
        podcastDataSource = (dataSourceFactory.getDataSource() as QueryPodcastDataSource)
    }

    fun activateManualModeAndNotify(queryString: String) {
        searchMode = SEARCH_MODE_MANUAL
        notifySearchModeManualActivated(queryString)
    }

    fun activateGenreModeAndNotify() {
        searchMode = SEARCH_MODE_GENRE
        notifySearchModGenreActivated()
    }

    fun restoreSearchModeAndNotify() {
        if (searchMode == SEARCH_MODE_GENRE) {
            activateGenreModeAndNotify()
        } else {
            notifySearchModeManualRestored()
        }
    }

    fun search(queryString: String) {
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

    private fun notifySearchModeManualActivated(queryString: String) {
        for (listener in listeners) {
            listener.onSearchModeManualActivated(queryString)
        }
    }

    private fun notifySearchModGenreActivated() {
        for (listener in listeners) {
            listener.onSearchModeGenreActivated()
        }
    }

    private fun notifySearchModeManualRestored() {
        for (listener in listeners) {
            listener.onSearchModeManualRestored()
        }
    }
}