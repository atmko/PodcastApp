package com.atmko.skiptoit.episodelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.database.EpisodeDao
import com.atmko.skiptoit.common.BaseBoundaryCallback

class EpisodeListViewModel(
    private val episodeDao: EpisodeDao,
    private val episodeBoundaryCallback: EpisodeBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : ViewModel() {

    companion object {
        const val pageSize = 10
        const val enablePlaceholders = true
        const val maxSize = 60
        const val prefetchDistance = 5
        const val initialLoadSize = 30
    }

    var episodes: LiveData<PagedList<Episode>>? = null

    fun getEpisodes(podcastId: String) {
        if (episodes != null
            && episodes!!.value != null
            && !episodes!!.value!!.isEmpty()
        ) {
            return
        }

        episodeBoundaryCallback.param = podcastId
        val dataSourceFactory = episodeDao.getAllEpisodesForPodcast(podcastId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Episode>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(episodeBoundaryCallback)
        episodes = pagedListBuilder.build()
    }

    fun registerBoundaryCallbackListener(listener: BaseBoundaryCallback.Listener) {
        episodeBoundaryCallback.registerListener(listener)
    }

    fun unregisterBoundaryCallbackListener(listener: BaseBoundaryCallback.Listener) {
        episodeBoundaryCallback.unregisterListener(listener)
    }
}