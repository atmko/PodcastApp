package com.atmko.skiptoit.search.searchchild

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory
import com.atmko.skiptoit.search.searchparent.GenrePodcastDataSource

class SearchViewModel(private val dataSourceFactory: PodcastDataSourceFactory) :
    BaseViewModel<SearchViewModel.Listener>() {

    interface Listener {
        fun onScrollPositionRestored(scrollPosition: Int)
    }

    companion object {
        const val SCROLL_POSITION_KEY = "scroll_position"
    }

    var scrollPosition: Int = 0

    private val pageSize = 20
    private val setInitialLoadSizeHint = 40

    lateinit var genreResults: LiveData<PagedList<Podcast>>

    //state save variables

    var podcastDataSource: GenrePodcastDataSource

    init {
        dataSourceFactory.setTypeClass(GenrePodcastDataSource::class.java)
        podcastDataSource = (dataSourceFactory.getDataSource() as GenrePodcastDataSource)
    }

    fun handleSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt(SCROLL_POSITION_KEY, 0)
        }

        notifyScrollPositionRestored()
    }

    fun saveState(outState: Bundle) {
        outState.putInt(SCROLL_POSITION_KEY, scrollPosition)
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

    private fun notifyScrollPositionRestored() {
        for (listener in listeners) {
            listener.onScrollPositionRestored(scrollPosition)
        }
    }
}