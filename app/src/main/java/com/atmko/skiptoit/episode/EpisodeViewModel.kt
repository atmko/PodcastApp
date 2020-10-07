package com.atmko.skiptoit.episode

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.EpisodesCache

class EpisodeViewModel(
    private val episodeEndpoint: EpisodeEndpoint,
    private val episodesCache: EpisodesCache
) : BaseViewModel<EpisodeViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onPodcastEpisodesCacheCleared()
        fun onPodcastEpisodesCacheClearFailed()
        fun onDetailsFetched(episode: Episode)
        fun onDetailsFetchFailed()
        fun onNextEpisodeFetched(episode: Episode)
        fun onNextEpisodeFetchFailed()
        fun onPreviousEpisodeFetched(episode: Episode)
        fun onPreviousEpisodeFetchFailed()
    }

    lateinit var episodeDetails: Episode
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    //if new podcast, erase previously played podcast's episodes from cache
    fun clearPodcastCacheAndNotify(currentPodcastId: String) {
        episodesCache.deletePodcastEpisodes(currentPodcastId, object : EpisodesCache.DeletePodcastEpisodesListener {
            override fun onDeletePodcastEpisodesSuccess() {
                notifyPodcastEpisodesCacheCleared()
            }

            override fun onDeletePodcastEpisodesFailed() {
                notifyPodcastEpisodesCacheClearFailed()
            }
        })
    }

    fun getDetailsAndNotify(episodeId: String) {
        notifyProcessing()

        if (this::episodeDetails.isInitialized) {
            notifyDetailsFetched(episodeDetails)
            return
        }

        episodeEndpoint.getEpisodeDetails(episodeId, object : EpisodeEndpoint.EpisodeDetailsListener {
            override fun onEpisodeDetailsFetchSuccess(episode: Episode) {
                episodeDetails = episode
                notifyDetailsFetched(episodeDetails)
            }

            override fun onEpisodeDetailsFetchFailed() {
                notifyDetailsFetchFailed()
            }
        })
    }

    fun restoreEpisodeAndNotify() {
        notifyProcessing()

        if (this::episodeDetails.isInitialized) {
            notifyDetailsFetched(episodeDetails)
            return
        }

        episodesCache.restoreEpisode(object : EpisodesCache.RestoreEpisodeListener {
            override fun onEpisodeRestoreSuccess(episode: Episode) {
                episodeDetails = episode
                notifyDetailsFetched(episodeDetails)
            }

            override fun onEpisodeRestoreFailed() {
                notifyDetailsFetchFailed()
            }
        })
    }

    var nextEpisode: Episode? = null
    var prevEpisode: Episode? = null

    fun fetchNextEpisodeAndNotify(podcastId: String, episode: Episode) {
        if (nextEpisode != null) {
            return
        }
        episodesCache.getNextEpisode(episode.episodeId, episode.publishDate, object : EpisodesCache.NextEpisodeListener {
            override fun onNextEpisodeFetchSuccess(cachedNextEpisode: Episode?) {
                nextEpisode = cachedNextEpisode
                if (nextEpisode != null) {
                    notifyNextEpisodeFetched(nextEpisode!!)
                } else {
                    fetchNextEpisodesFromRemoteAndNotify(podcastId, episode.publishDate)
                }
            }

            override fun onNextEpisodeFetchFailed() {
                notifyNextEpisodeFetchFailed()
            }
        })
    }

    fun fetchPrevEpisodeAndNotify(episode: Episode) {
        if (prevEpisode != null) {
            return
        }
        episodesCache.getPreviousEpisode(episode.episodeId, episode.publishDate, object : EpisodesCache.PreviousEpisodeListener {
            override fun onPreviousEpisodeFetchSuccess(cachedPreviousEpisode: Episode?) {
                prevEpisode = cachedPreviousEpisode
                if (prevEpisode != null) {
                    notifyPreviousEpisodeFetched(prevEpisode!!)
                }
            }

            override fun onPreviousEpisodeFetchFailed() {
                notifyPreviousEpisodeFetchFailed()
            }
        })
    }

    private fun fetchNextEpisodesFromRemoteAndNotify(podcastId: String, episodePublishDate: Long) {
        episodeEndpoint.fetchNextEpisodes(podcastId, episodePublishDate, object : EpisodeEndpoint.BatchNextEpisodeListener {
            override fun onBatchNextEpisodesFetchSuccess(podcastDetails: PodcastDetails) {
                if (podcastDetails.episodes.isNotEmpty()) {
                    episodesCache.insertEpisodesAndReturnNextEpisode(podcastDetails, object : EpisodesCache.NextEpisodeListener {
                        override fun onNextEpisodeFetchSuccess(cachedNextEpisode: Episode?) {
                            nextEpisode = cachedNextEpisode
                            if (nextEpisode != null) {
                                notifyNextEpisodeFetched(nextEpisode!!)
                            }
                        }

                        override fun onNextEpisodeFetchFailed() {
                            notifyNextEpisodeFetchFailed()
                        }
                    })
                }
            }

            override fun onBatchNextEpisodesFetchFailed() {
                notifyNextEpisodeFetchFailed()
            }
        })
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    private fun notifyProcessing() {
        for (listener in listeners) {
            listener.notifyProcessing()
        }
    }

    private fun notifyPodcastEpisodesCacheCleared() {
        for (listener in listeners) {
            listener.onPodcastEpisodesCacheCleared()
        }
    }

    private fun notifyPodcastEpisodesCacheClearFailed() {
        for (listener in listeners) {
            listener.onPodcastEpisodesCacheClearFailed()
        }
    }

    private fun notifyDetailsFetched(episodeDetails: Episode) {
        for (listener in listeners) {
            listener.onDetailsFetched(episodeDetails)
        }
    }

    private fun notifyDetailsFetchFailed() {
        for (listener in listeners) {
            listener.onDetailsFetchFailed()
        }
    }

    private fun notifyNextEpisodeFetched(episode: Episode) {
        for (listener in listeners) {
            listener.onNextEpisodeFetched(episode)
        }
    }

    private fun notifyNextEpisodeFetchFailed() {
        for (listener in listeners) {
            listener.onNextEpisodeFetchFailed()
        }
    }

    private fun notifyPreviousEpisodeFetched(episode: Episode) {
        for (listener in listeners) {
            listener.onPreviousEpisodeFetched(episode)
        }
    }

    private fun notifyPreviousEpisodeFetchFailed() {
        for (listener in listeners) {
            listener.onPreviousEpisodeFetchFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}