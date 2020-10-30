package com.atmko.skiptoit.model.database

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.atmko.skiptoit.common.BaseBoundaryCallback.Companion.loadTypeRefresh
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.utils.AppExecutors

open class EpisodesCache(
    private val skipToItDatabase: SkipToItDatabase?,
    private val prefs: SharedPreferences?
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

    open fun insertEpisodesAndReturnNextEpisode(
        podcastDetails: PodcastDetails,
        listener: NextEpisodeListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            skipToItDatabase!!.beginTransaction()
            try {
                val episodes = podcastDetails.episodes
                for (i in episodes) {
                    i.podcastId = podcastDetails.id
                }

                skipToItDatabase.episodeDao().insertEpisodes(episodes)
                skipToItDatabase.setTransactionSuccessful()

                AppExecutors.getInstance().mainThread().execute {
                    listener.onNextEpisodeFetchSuccess(episodes[0])
                }
            } finally {
                skipToItDatabase.endTransaction()
                AppExecutors.getInstance().mainThread().execute {
                    listener.onNextEpisodeFetchFailed()
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
        AppExecutors.getInstance().diskIO().execute {
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

                AppExecutors.getInstance().mainThread().execute {
                    listener.onPageFetchSuccess()
                }
            } finally {
                skipToItDatabase.endTransaction()
                AppExecutors.getInstance().mainThread().execute {
                    listener.onPageFetchFailed()
                }
            }
        }
    }

    open fun getAllPodcastEpisodes(podcastId: String, listener: GetAllPodcastEpisodesListener) {
        AppExecutors.getInstance().diskIO().execute {
            val podcastEpisodes =
                skipToItDatabase!!.episodeDao().getAllPodcastEpisodesAlt(podcastId)
            AppExecutors.getInstance().mainThread().execute {
                listener.onGetAllEpisodesSuccess(podcastEpisodes)
            }
        }
    }

    open fun deletePodcastEpisodes(
        currentPodcastId: String,
        listener: DeletePodcastEpisodesListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            val lastPlayedPodcastId = prefs!!.getString(PODCAST_ID_KEY, "")
            if (lastPlayedPodcastId != "" && lastPlayedPodcastId != currentPodcastId) {
                skipToItDatabase!!.episodeDao().deletePodcastEpisodes(currentPodcastId)
            }
            AppExecutors.getInstance().mainThread().execute {
                listener.onDeletePodcastEpisodesSuccess()
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    open fun saveEpisode(episode: Episode, listener: SaveEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            prefs!!.edit()
                .putString(PODCAST_ID_KEY, episode.podcastId)
                .putString(EPISODE_ID_KEY, episode.episodeId)
                .putString(PODCAST_TITLE_KEY, episode.podcast!!.title)
                .commit()

            AppExecutors.getInstance().mainThread().execute {
                listener.onEpisodeSaveSuccess()
            }
        }
    }

    open fun restoreEpisode(listener: RestoreEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            val episodeId = prefs!!.getString(EPISODE_ID_KEY, "")
            val podcastId = prefs.getString(PODCAST_ID_KEY, "")
            val podcastTitle = prefs.getString(PODCAST_TITLE_KEY, "")

            val restoredEpisode: Episode? = skipToItDatabase!!.episodeDao().getEpisode(episodeId!!)
            if (restoredEpisode != null) {
                restoredEpisode.podcastId = podcastId
                val podcast =
                    Podcast(podcastId!!, podcastTitle, "", "", "", 0)
                restoredEpisode.podcast = podcast
            }

            AppExecutors.getInstance().mainThread().execute {
                listener.onEpisodeRestoreSuccess(restoredEpisode)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    open fun clearLastPlayedEpisode(listener: ClearLastPlayedEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            prefs!!.edit()
                .remove(PODCAST_ID_KEY)
                .remove(EPISODE_ID_KEY)
                .remove(PODCAST_TITLE_KEY)
                .commit()

            AppExecutors.getInstance().mainThread().execute {
                listener.onEpisodeClearSuccess()
            }
        }
    }

    open fun getNextEpisode(episodeId: String, publishDate: Long, listener: NextEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            val episode = skipToItDatabase!!.episodeDao().getNextEpisode(episodeId, publishDate)

            AppExecutors.getInstance().mainThread().execute {
                listener.onNextEpisodeFetchSuccess(episode)
            }
        }
    }

    open fun getPreviousEpisode(
        episodeId: String,
        publishDate: Long,
        listener: PreviousEpisodeListener
    ) {
        AppExecutors.getInstance().diskIO().execute {
            val episode = skipToItDatabase!!.episodeDao().getPrevEpisode(episodeId, publishDate)

            AppExecutors.getInstance().mainThread().execute {
                listener.onPreviousEpisodeFetchSuccess(episode)
            }
        }
    }
}