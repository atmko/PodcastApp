package com.atmko.skiptoit.search.searchchild

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.search.searchparent.GenrePodcastDataSource
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.search.common.PodcastDataSource

class SearchViewModel(private val dataSourceFactory: PodcastDataSourceFactory) : ViewModel() {

    private val pageSize = 20
    private val setInitialLoadSizeHint = 40

    lateinit var genreResults: LiveData<PagedList<Podcast>>

    //state save variables
    var scrollPosition: Int = 0

    var podcastDataSource: GenrePodcastDataSource

    init {
        dataSourceFactory.setTypeClass(GenrePodcastDataSource::class.java)
        podcastDataSource = (dataSourceFactory.getDataSource() as GenrePodcastDataSource)
    }

    fun fetchPodcastsByGenre(genreId: Int) {
        if (this::genreResults.isInitialized && genreResults.value != null) {
            return
        }

        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(setInitialLoadSizeHint)
            .build()
        podcastDataSource.genreId = genreId
        genreResults = LivePagedListBuilder<Int, Podcast>(dataSourceFactory, config)
            .build()
    }

    fun registerBoundaryCallbackListener(listener: PodcastDataSource.Listener) {
        podcastDataSource.registerListener(listener)
    }

    fun unregisterBoundaryCallbackListener(listener: PodcastDataSource.Listener) {
        podcastDataSource.unregisterListener(listener)
    }
}