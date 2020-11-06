package com.atmko.skiptoit.episode

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache

class EpisodeViewModel(
    private val episodeEndpoint: EpisodeEndpoint,
    private val episodesCache: EpisodesCache
) : BaseViewModel<EpisodeViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onDetailsFetched(episode: Episode?, isRestoringEpisode: Boolean)
        fun onDetailsFetchFailed()
        fun onNextEpisodeFetched(episode: Episode)
        fun onNextEpisodeFetchFailed()
        fun onPreviousEpisodeFetched(episode: Episode)
        fun onPreviousEpisodeFetchFailed()
    }

    var episodeDetails: Episode? = null
    var nextEpisode: Episode? = null
    var prevEpisode: Episode? = null

    fun getDetailsAndNotify(episodeId: String?, podcastId: String?) {
        if (episodeDetails != null) {
            notifyDetailsFetched(episodeDetails, true)
            return
        }

        notifyProcessing()

        if (episodeId != null && podcastId != null) {
            clearPodcastCache(podcastId, episodeId)
        } else {
            restoreEpisode()
        }
    }

    //if new podcast, erase previously played podcast's episodes from cache
    private fun clearPodcastCache(currentPodcastId: String, episodeId: String) {
        episodesCache.deletePodcastEpisodes(
            currentPodcastId,
            object : EpisodesCache.DeletePodcastEpisodesListener {
                override fun onDeletePodcastEpisodesSuccess() {
                    getEpisodeDetails(currentPodcastId, episodeId)
                }

                override fun onDeletePodcastEpisodesFailed() {
                    notifyDetailsFetchFailed()
                }
            })
    }

    private fun getEpisodeDetails(podcastId: String, episodeId: String) {
        episodeEndpoint.getEpisodeDetails(
            episodeId,
            object : EpisodeEndpoint.EpisodeDetailsListener {
                override fun onEpisodeDetailsFetchSuccess(episode: Episode) {
                    episode.podcastId = podcastId
                    saveEpisode(episode)
                }

                override fun onEpisodeDetailsFetchFailed() {
                    notifyDetailsFetchFailed()
                }
            })
    }

    private fun saveEpisode(episode: Episode) {
        episodesCache.saveEpisode(episode, object : EpisodesCache.SaveEpisodeListener {
            override fun onEpisodeSaveSuccess() {
                episodeDetails = episode
                notifyDetailsFetched(episodeDetails, false)
            }

            override fun onEpisodeSaveFailed() {
                notifyDetailsFetchFailed()
            }
        })
    }

    private fun restoreEpisode() {
        episodesCache.restoreEpisode(object : EpisodesCache.RestoreEpisodeListener {
            override fun onEpisodeRestoreSuccess(episode: Episode?) {
                episodeDetails = episode
                notifyDetailsFetched(episodeDetails, true)
            }

            override fun onEpisodeRestoreFailed() {
                notifyDetailsFetchFailed()
            }
        })
    }

    fun fetchNextEpisodeAndNotify(episodeDetails: Episode) {
        if (nextEpisode != null) {
            return
        }
        episodesCache.getNextEpisode(
            episodeDetails.podcastId!!,
            episodeDetails.episodeId,
            episodeDetails.publishDate,
            object : EpisodesCache.NextEpisodeListener {
                override fun onNextEpisodeFetchSuccess(cachedNextEpisode: Episode?) {
                    nextEpisode = cachedNextEpisode
                    if (nextEpisode != null) {
                        notifyNextEpisodeFetched(nextEpisode!!)
                    } else {
                        fetchNextEpisodesFromRemoteAndNotify(
                            episodeDetails.podcastId!!,
                            episodeDetails.publishDate
                        )
                    }
                }

                override fun onNextEpisodeFetchFailed() {
                    notifyNextEpisodeFetchFailed()
                }
            })
    }

    fun fetchPrevEpisodeAndNotify(episodeDetails: Episode) {
        if (prevEpisode != null) {
            return
        }
        episodesCache.getPreviousEpisode(
            episodeDetails.podcastId!!,
            episodeDetails.episodeId,
            episodeDetails.publishDate,
            object : EpisodesCache.PreviousEpisodeListener {
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
        episodeEndpoint.fetchNextEpisodes(
            podcastId,
            episodePublishDate,
            object : EpisodeEndpoint.BatchNextEpisodeListener {
                override fun onBatchNextEpisodesFetchSuccess(podcastDetails: PodcastDetails) {
                    if (podcastDetails.episodes.isNotEmpty()) {
                        episodesCache.insertEpisodesAndReturnNextEpisode(
                            podcastDetails,
                            object : EpisodesCache.NextEpisodeListener {
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

    private fun notifyDetailsFetched(episodeDetails: Episode?, isRestored: Boolean) {
        for (listener in listeners) {
            listener.onDetailsFetched(episodeDetails, isRestored)
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