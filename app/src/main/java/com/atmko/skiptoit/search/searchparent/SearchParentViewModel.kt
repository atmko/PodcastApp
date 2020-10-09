package com.atmko.skiptoit.search.searchparent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource
import com.atmko.skiptoit.common.PodcastDataSourceFactory
import com.atmko.skiptoit.search.common.PodcastDataSource

class SearchParentViewModel(private val dataSourceFactory: PodcastDataSourceFactory) : ViewModel() {

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
}