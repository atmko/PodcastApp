package com.atmko.skiptoit.details

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.EpisodesCache

class DetailsViewModel(
    private val podcastDetailsEndpoint: PodcastDetailsEndpoint,
    private val episodesCache: EpisodesCache
) : BaseViewModel<DetailsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onDetailsFetched(podcastDetails: PodcastDetails)
        fun onDetailsFetchFailed()

        fun onOldPodcastDetected(episodeId: String)
        fun onNewPodcastDetected()
        fun onPodcastDetectFailed()

        fun onToggleOldEpisodePlayback()
        fun onToggleOldEpisodePlaybackFailed()
        fun onToggleNewEpisodePlayback(episodeId: String)
        fun onToggleNewEpisodePlaybackFailed()

        fun onExpandOldListedEpisode()
        fun onExpandNewListedEpisode(episodeId: String)
    }

    lateinit var podcastDetails: PodcastDetails
    var currentEpisodeId: String? = null

    fun getDetailsAndNotify(podcastId: String) {
        if (this::podcastDetails.isInitialized) {
            notifyDetailsFetched(podcastDetails)
            return
        }

        notifyProcessing()

        podcastDetailsEndpoint.getPodcastDetails(
            podcastId,
            object : PodcastDetailsEndpoint.Listener {
                override fun onPodcastDetailsFetchSuccess(fetchedPodcastDetails: PodcastDetails) {
                    podcastDetails = fetchedPodcastDetails
                    notifyDetailsFetched(fetchedPodcastDetails)
                }

                override fun onPodcastDetailsFetchFailed() {
                    notifyDetailsFetchFailed()
                }
            })
    }

    var isOldPodcast: Boolean? = null
    fun detectOldOrNewPodcastAndNotify(podcastId: String) {
        episodesCache.restoreEpisode(object : EpisodesCache.RestoreEpisodeListener {
            override fun onEpisodeRestoreSuccess(episode: Episode?) {
                isOldPodcast = episode != null && podcastId == episode.podcastId
                if (isOldPodcast!!) {
                    currentEpisodeId = episode?.episodeId
                    notifyOldPodcastDetected()
                } else {
                    notifyNewPodcastDetected()
                }
            }

            override fun onEpisodeRestoreFailed() {
                notifyDetectPodcastFailed()
            }
        })
    }

    fun togglePlayButtonAndNotify(podcastId: String) {
        // notify failure if isOldPodcast hasn't been determined
        if (isOldPodcast == null) {
            notifyToggleOldEpisodePlaybackFailed()
            return
        }

        // if isOldPodcast means that it been determined that there must be a currently loaded episode. toggle old episode playback
        if (isOldPodcast!!) {
            notifyToggleOldEpisodePlayback()
            return
        }

        // toggle current episode if it exists
        if (currentEpisodeId != null) {
            notifyToggleOldEpisodePlayback()
            return
        }

        episodesCache.getAllPodcastEpisodes(
            podcastId,
            object : EpisodesCache.GetAllPodcastEpisodesListener {
                override fun onGetAllEpisodesSuccess(podcastEpisodes: List<Episode>) {
                    isOldPodcast = true
                    currentEpisodeId =
                        if (podcastEpisodes.isNotEmpty()) podcastEpisodes[0].episodeId else null

                    if (currentEpisodeId != null) {
                        notifyToggleNewEpisodePlayback()
                    } else {
                        notifyToggleNewEpisodePlaybackFailed()
                    }
                }

                override fun onGetAllEpisodesFailed() {
                    notifyToggleNewEpisodePlaybackFailed()
                }
            })
    }

    fun toggleListedEpisodePlaybackAndNotify(episodeId: String) {
        if (isOldEpisode(episodeId)) {
            notifyToggleOldEpisodePlayback()
        } else {
            currentEpisodeId = episodeId
            notifyToggleNewEpisodePlayback()
        }
    }

    fun expandListedEpisodeAndNotify(episodeId: String) {
        if (isOldEpisode(episodeId)) {
            notifyExpandOldListedEpisode()
        } else {
            currentEpisodeId = episodeId
            notifyExpandNewListedEpisode()
        }
    }

    private fun isOldEpisode(episodeId: String): Boolean {
        return currentEpisodeId == episodeId
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

    private fun notifyDetailsFetched(podcastDetails: PodcastDetails) {
        for (listener in listeners) {
            listener.onDetailsFetched(podcastDetails)
        }
    }

    private fun notifyDetailsFetchFailed() {
        for (listener in listeners) {
            listener.onDetailsFetchFailed()
        }
    }

    private fun notifyOldPodcastDetected() {
        for (listener in listeners) {
            listener.onOldPodcastDetected(currentEpisodeId!!)
        }
    }

    private fun notifyNewPodcastDetected() {
        for (listener in listeners) {
            listener.onNewPodcastDetected()
        }
    }

    private fun notifyDetectPodcastFailed() {
        for (listener in listeners) {
            listener.onPodcastDetectFailed()
        }
    }

    private fun notifyToggleOldEpisodePlayback() {
        for (listener in listeners) {
            listener.onToggleOldEpisodePlayback()
        }
    }

    private fun notifyToggleOldEpisodePlaybackFailed() {
        for (listener in listeners) {
            listener.onToggleOldEpisodePlaybackFailed()
        }
    }

    private fun notifyToggleNewEpisodePlayback() {
        for (listener in listeners) {
            listener.onToggleNewEpisodePlayback(currentEpisodeId!!)
        }
    }

    private fun notifyToggleNewEpisodePlaybackFailed() {
        for (listener in listeners) {
            listener.onToggleNewEpisodePlaybackFailed()
        }
    }

    private fun notifyExpandOldListedEpisode() {
        for (listener in listeners) {
            listener.onExpandOldListedEpisode()
        }
    }

    private fun notifyExpandNewListedEpisode() {
        for (listener in listeners) {
            listener.onExpandNewListedEpisode(currentEpisodeId!!)
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}