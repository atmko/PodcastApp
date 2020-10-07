package com.atmko.skiptoit.model.database

import android.content.SharedPreferences
import com.atmko.skiptoit.utils.AppExecutors
import com.atmko.skiptoit.common.BaseBoundaryCallback.Companion.loadTypeRefresh
import com.atmko.skiptoit.model.*

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

    interface RestoreEpisodeListener {
        fun onEpisodeRestoreSuccess(episode: Episode)
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
                    skipToItDatabase.episodeDao().deleteAllEpisodesExceptNowPlaying(lastPlayedPodcastId)
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

    open fun deletePodcastEpisodes(currentPodcastId: String, listener: DeletePodcastEpisodesListener) {
        AppExecutors.getInstance().diskIO().execute {
            val lastPlayedPodcastId = prefs!!.getString(PODCAST_ID_KEY, "")
            if (lastPlayedPodcastId != null && lastPlayedPodcastId != currentPodcastId) {
                skipToItDatabase!!.episodeDao().deletePodcastEpisodes(currentPodcastId)
            }
            AppExecutors.getInstance().mainThread().execute {
                listener.onDeletePodcastEpisodesSuccess()
            }
        }
    }

    open fun restoreEpisode(listener: RestoreEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            val podcastId = prefs!!.getString(PODCAST_ID_KEY, "")!!
            val episodeId = prefs.getString(EPISODE_ID_KEY, "")
            val title = prefs.getString(EPISODE_TITLE_KEY, "")
            val description = prefs.getString(EPISODE_DESCRIPTION_KEY, "")
            val image = prefs.getString(EPISODE_IMAGE_KEY, "")
            val audio = prefs.getString(EPISODE_AUDIO_KEY, "")
            val publishDate = prefs.getLong(EPISODE_PUBLISH_DATE_KEY, 0)
            val lengthInSeconds = prefs.getInt(EPISODE_LENGTH_IN_SECONDS_KEY, 0)

            val podcastTitle = prefs.getString(PODCAST_TITLE_KEY, "")

            val podcast =
                Podcast(podcastId, podcastTitle, "", "", "", 0)

            val episode =
                Episode(
                    episodeId!!,
                    title,
                    description,
                    image,
                    audio,
                    publishDate,
                    lengthInSeconds,
                    podcast
                )

            episode.podcastId = podcast.id

            AppExecutors.getInstance().mainThread().execute {
                listener.onEpisodeRestoreSuccess(episode)
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

    open fun getPreviousEpisode(episodeId: String, publishDate: Long, listener: PreviousEpisodeListener) {
        AppExecutors.getInstance().diskIO().execute {
            val episode = skipToItDatabase!!.episodeDao().getPrevEpisode(episodeId, publishDate)

            AppExecutors.getInstance().mainThread().execute {
                listener.onPreviousEpisodeFetchSuccess(episode)
            }
        }
    }
}