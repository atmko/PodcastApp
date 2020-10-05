package com.atmko.skiptoit.model.database

import android.content.SharedPreferences
import com.atmko.skiptoit.model.PODCAST_ID_KEY
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.utils.AppExecutors
import com.atmko.skiptoit.common.BaseBoundaryCallback.Companion.loadTypeRefresh

open class EpisodesCache(
    private val skipToItDatabase: SkipToItDatabase?,
    private val prefs: SharedPreferences?
) {

    interface PageFetchListener {
        fun onPageFetchSuccess()
        fun onPageFetchFailed()
    }

    open fun insertEpisodes(
        podcastDetails: PodcastDetails,
        loadType: Int,
        param: String,
        listener: PageFetchListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == loadTypeRefresh) {
                    val lastPlayedPodcastId = prefs!!.getString(PODCAST_ID_KEY, "")!!
                    skipToItDatabase.episodeDao().deleteAllEpisodesExceptNowPlaying(lastPlayedPodcastId)
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

            AppExecutors.getInstance().mainThread().execute {
                listener.onPageFetchSuccess()
            }
        }
    }
}