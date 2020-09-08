package com.atmko.skiptoit.viewmodel.paging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors

class EpisodeBoundaryCallback(
    private val podcastsApi: PodcastsApi,
    private val skipToItDatabase: SkipToItDatabase
): PagedList.BoundaryCallback<Episode>() {

    private val tag = this::class.simpleName

    private val loadTypeRefresh = 0
    private val loadTypeAppend = 1

    lateinit var param: String
    var startPage: Long? = null

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    
    override fun onZeroItemsLoaded() {
        Log.d("REFRESHING","REFRESHING")
        AppExecutors.getInstance().diskIO().execute {
            requestPage(loadTypeRefresh, startPage)
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Episode) {
        Log.d(tag,"APPEND")
        AppExecutors.getInstance().diskIO().execute {
            requestPage(loadTypeAppend, itemAtEnd.publishDate)
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Episode) {

    }

    private fun requestPage(requestType: Int, loadKey: Long?) {
        val episodeResultCall = podcastsApi.getEpisodes(param, loadKey)

        val response = episodeResultCall.execute()
        if (response.isSuccessful) {
            Log.d(tag, "isSuccessful")
            loading.postValue(false)
            loadError.postValue(false)

            val body: PodcastDetails = response.body()!!
            onEpisodeFetchCallback(body, requestType)
        } else {
            Log.d(tag, "!isSuccessful")
            loading.postValue(false)
            loadError.postValue(true)
        }
    }

    private fun onEpisodeFetchCallback(
        podcastDetails: PodcastDetails,
        loadType: Int
    ) {

        skipToItDatabase.beginTransaction()
        try {
            if (loadType == loadTypeRefresh) {
                skipToItDatabase.episodeDao().deleteAllEpisodes()
            }

            val episodes = podcastDetails.episodes
            for (i in episodes) {
                i.podcastId = param
            }

            skipToItDatabase.episodeDao().insertEpisodes(episodes)
            skipToItDatabase.setTransactionSuccessful()
        } finally {
            skipToItDatabase.endTransaction()
        }
    }
}