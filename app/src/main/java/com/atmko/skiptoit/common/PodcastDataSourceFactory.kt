package com.atmko.skiptoit.common

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.search.common.PodcastDataSource
import javax.inject.Provider

class PodcastDataSourceFactory(
    private val providerMap: Map<Class<out PodcastDataSource>, Provider<PodcastDataSource>>
) :
    DataSource.Factory<Int, Podcast>() {

    lateinit var podcastDataSource: DataSource<Int, Podcast>

    private val genrePodcastDataSourceLiveData: MutableLiveData<DataSource<Int, Podcast>> =
        MutableLiveData()

    fun <T : PodcastDataSource> setTypeClass(type: Class<T>) {
        podcastDataSource = providerMap[type]?.get() as PodcastDataSource
    }

    override fun create(): DataSource<Int, Podcast> {
        genrePodcastDataSourceLiveData.postValue(podcastDataSource)
        return podcastDataSource
    }

    fun getDataSource(): DataSource<Int, Podcast> {
        return podcastDataSource
    }
}