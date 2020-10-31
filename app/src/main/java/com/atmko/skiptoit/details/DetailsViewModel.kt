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

        fun onOldPodcastDetced()
        fun onNewPodcastDetcted()
        fun onPodcastDetectFailed()

        fun onToggleOldEpisodePlayback()
        fun onToggleOldEpisodePlaybackFailed()
        fun onToggleNewEpisodePlayback(latestEpisodeId: String)
        fun onToggleNewEpisodePlaybackFailed()
    }

    lateinit var podcastDetails: PodcastDetails
    var latestEpisodeId: String? = null

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

    fun togglePlaybackAndNotify(podcastId: String) {
        if (latestEpisodeId != null) {
            notifyToggleOldEpisodePlayback()
            return
        }

        if (isOldPodcast == null ) {
            notifyToggleOldEpisodePlaybackFailed()
            return
        }

        if (isOldPodcast!!) {
            notifyToggleOldEpisodePlayback()
            return
        }

        episodesCache.getAllPodcastEpisodes(
            podcastId,
            object : EpisodesCache.GetAllPodcastEpisodesListener {
                override fun onGetAllEpisodesSuccess(podcastEpisodes: List<Episode>) {
                    latestEpisodeId =
                        if (podcastEpisodes.isNotEmpty()) podcastEpisodes[0].episodeId else null

                    if (latestEpisodeId != null) {
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
            listener.onOldPodcastDetced()
        }
    }

    private fun notifyNewPodcastDetected() {
        for (listener in listeners) {
            listener.onNewPodcastDetcted()
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
            listener.onToggleNewEpisodePlayback(latestEpisodeId!!)
        }
    }

    private fun notifyToggleNewEpisodePlaybackFailed() {
        for (listener in listeners) {
            listener.onToggleNewEpisodePlaybackFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}