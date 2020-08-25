package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.viewmodel.datasource.GenrePodcastDataSource
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.viewmodel.common.PodcastDataSourceFactory
import java.util.concurrent.Executors

class SearchViewModel(private val dataSourceFactory: PodcastDataSourceFactory) : ViewModel() {

    private val pageSize = 20
    private val setInitialLoadSizeHint = 40

    lateinit var genreResults: LiveData<PagedList<Podcast>>

    //state save variables
    var scrollPosition: Int = 0

    fun fetchPodcastsByGenre(genreId: Int) {
        if (this::genreResults.isInitialized && genreResults.value != null) {
            return
        }

        val config = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(setInitialLoadSizeHint)
            .build()
        dataSourceFactory.setTypeClass(GenrePodcastDataSource::class.java)
        (dataSourceFactory.getDataSource() as GenrePodcastDataSource).genreId = genreId
        genreResults = LivePagedListBuilder<Int, Podcast>(dataSourceFactory, config)
            .setFetchExecutor(Executors.newFixedThreadPool(5))
            .build()
    }

    fun getGenreLoading(): LiveData<Boolean> {
        return (dataSourceFactory.getDataSource() as GenrePodcastDataSource).loading
    }

    fun getGenreLoadError(): LiveData<Boolean> {
        return (dataSourceFactory.getDataSource() as GenrePodcastDataSource).loadError
    }
}