package com.atmko.skiptoit.model.database

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.atmko.skiptoit.common.BaseBoundaryCallback.Companion.loadTypeRefresh
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.utils.AppExecutors

open class EpisodesCache(
    private val skipToItDatabase: SkipToItDatabase?,
    private val prefs: SharedPreferences?,
    private val appExecutors: AppExecutors?
) {

    interface PageFetchListener {
        fun onPageFetchSuccess()
        fun onPageFetchFailed()
    }

    interface DeletePodcastEpisodesListener {
        fun onDeletePodcastEpisodesSuccess()
        fun onDeletePodcastEpisodesFailed()
    }

    interface SaveEpisodeListener {
        fun onEpisodeSaveSuccess()
        fun onEpisodeSaveFailed()
    }

    interface ClearLastPlayedEpisodeListener {
        fun onEpisodeClearSuccess()
        fun onEpisodeClearFailed()
    }

    interface RestoreEpisodeListener {
        fun onEpisodeRestoreSuccess(episode: Episode?)
        fun onEpisodeRestoreFailed()
    }

    interface NextEpisodeListener {
        fun onNextEpisodeFetchSuccess(cachedNextEpisode: Episode?)
        fun onNextEpisodeFetchFailed()
    }

    interface PreviousEpisodeListener {
        fun onPreviousEpisodeFetchSuccess(cachedPreviousEpisode: Episode?)
        fun onPreviousEpisodeFetchFailed()
    }

    interface GetAllPodcastEpisodesListener {
        fun onGetAllEpisodesSuccess(podcastEpisodes: List<Episode>)
        fun onGetAllEpisodesFailed()
    }

    open fun insertEpisodesAndReturnPrevEpisode(
        podcastDetails: PodcastDetails,
        listener: PreviousEpisodeListener
    ) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.beginTransaction()
            try {
                val episodes = podcastDetails.episodes
                // todo: remove processing from here to view model
                for (i in episodes) {
                    i.podcastId = podcastDetails.id
                }

                skipToItDatabase.episodeDao().insertEpisodes(episodes)
                skipToItDatabase.setTransactionSuccessful()

                appExecutors.mainThread.execute {
                    listener.onPreviousEpisodeFetchSuccess(episodes[0])
                }
            } finally {
                skipToItDatabase.endTransaction()
                appExecutors.mainThread.execute {
                    listener.onPreviousEpisodeFetchFailed()
                }
            }
        }
    }

    open fun insertEpisodesForPaging(
        podcastDetails: PodcastDetails,
        loadType: Int,
        param: String,
        listener: PageFetchListener
    ) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.beginTransaction()
            try {
                if (loadType == loadTypeRefresh) {
                    val lastPlayedPodcastId = prefs!!.getString(PODCAST_ID_KEY, "")!!
                    skipToItDatabase.episodeDao()
                        .deleteAllEpisodesExceptNowPlaying(lastPlayedPodcastId)
                }

                val episodes = podcastDetails.episodes
                for (i in episodes) {
                    i.podcastId = param
                }

                skipToItDatabase.episodeDao().insertEpisodes(episodes)
                skipToItDatabase.setTransactionSuccessful()

            } finally {
                skipToItDatabase.endTransaction()
                appExecutors.mainThread.execute {
                    listener.onPageFetchSuccess()
                }
            }
        }
    }

    open fun getAllPodcastEpisodes(podcastId: String, listener: GetAllPodcastEpisodesListener) {
        appExecutors!!.diskIO.execute {
            val podcastEpisodes =
                skipToItDatabase!!.episodeDao().getAllPodcastEpisodesAlt(podcastId)
            appExecutors.mainThread.execute {
                listener.onGetAllEpisodesSuccess(podcastEpisodes)
            }
        }
    }

    open fun deletePodcastEpisodes(
        currentPodcastId: String,
        listener: DeletePodcastEpisodesListener
    ) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.episodeDao().deleteAllEpisodesExceptNowPlaying(currentPodcastId)
            appExecutors.mainThread.execute {
                listener.onDeletePodcastEpisodesSuccess()
            }
        }
    }

    open fun saveEpisodeWithListener(episode: Episode, listener: SaveEpisodeListener) {
        appExecutors!!.diskIO.execute {
            saveEpisodeHelper(episode)

            appExecutors.mainThread.execute {
                listener.onEpisodeSaveSuccess()
            }
        }
    }

    open fun saveEpisode(episode: Episode) {
        saveEpisodeHelper(episode)
    }

    @SuppressLint("ApplySharedPref")
    private fun saveEpisodeHelper(episode: Episode) {
        prefs!!.edit()
            .putString(PODCAST_ID_KEY, episode.podcastId)
            .putString(EPISODE_ID_KEY, episode.episodeId)
            .putString(PODCAST_TITLE_KEY, episode.podcast!!.title)
            .putLong(LAST_PLAYBACK_POSITION_KEY, episode.lastPlaybackPosition)
            .commit()
    }

    open fun restoreEpisode(listener: RestoreEpisodeListener) {
        appExecutors!!.diskIO.execute {
            val episodeId = prefs!!.getString(EPISODE_ID_KEY, "")
            val podcastId = prefs.getString(PODCAST_ID_KEY, "")
            val podcastTitle = prefs.getString(PODCAST_TITLE_KEY, "")
            val lastPlaybackPosition = prefs.getLong(LAST_PLAYBACK_POSITION_KEY, 0)

            val restoredEpisode: Episode? = skipToItDatabase!!.episodeDao().getEpisode(episodeId!!)
            if (restoredEpisode != null) {
                restoredEpisode.podcastId = podcastId
                val podcast =
                    Podcast(podcastId!!, podcastTitle, "", "", "", 0)
                restoredEpisode.podcast = podcast
                restoredEpisode.lastPlaybackPosition = lastPlaybackPosition
            }

            appExecutors.mainThread.execute {
                listener.onEpisodeRestoreSuccess(restoredEpisode)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    open fun clearLastPlayedEpisode(listener: ClearLastPlayedEpisodeListener) {
        appExecutors!!.diskIO.execute {
            prefs!!.edit()
                .remove(PODCAST_ID_KEY)
                .remove(EPISODE_ID_KEY)
                .remove(PODCAST_TITLE_KEY)
                .commit()

            appExecutors.mainThread.execute {
                listener.onEpisodeClearSuccess()
            }
        }
    }

    open fun getNextEpisode(
        podcastId: String,
        episodeId: String,
        publishDate: Long,
        listener: NextEpisodeListener
    ) {
        appExecutors!!.diskIO.execute {
            val episode =
                skipToItDatabase!!.episodeDao().getNextEpisode(podcastId, episodeId, publishDate)

            appExecutors.mainThread.execute {
                listener.onNextEpisodeFetchSuccess(episode)
            }
        }
    }

    open fun getPreviousEpisode(
        podcastId: String,
        episodeId: String,
        publishDate: Long,
        listener: PreviousEpisodeListener
    ) {
        appExecutors!!.diskIO.execute {
            val episode =
                skipToItDatabase!!.episodeDao().getPrevEpisode(podcastId, episodeId, publishDate)

            appExecutors.mainThread.execute {
                listener.onPreviousEpisodeFetchSuccess(episode)
            }
        }
    }
}