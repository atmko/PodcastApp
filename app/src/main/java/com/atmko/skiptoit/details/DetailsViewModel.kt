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
        fun onIsLastPlayedPodcastFetched(isLastPlayedPodcast: Boolean)
        fun onIsLastPlayedPodcastFetchFailed()
        fun onLatestEpisodeIdFetched(firstEpisodeId: String?)
        fun onLatestEpisodeIdFetchFailed()
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

    fun checkIsLastPlayedPodcastAndNotify(podcastId: String) {
        episodesCache.restoreEpisode(object : EpisodesCache.RestoreEpisodeListener {
            override fun onEpisodeRestoreSuccess(episode: Episode?) {
                notifyIsLastPlayedPodcastFetched(
                    episode != null && podcastId == episode.podcastId
                )
            }

            override fun onEpisodeRestoreFailed() {
                notifyIsLastPlayedPodcastFetchFailed()
            }
        })
    }

    fun getLatestEpisodeIdAndNotify(podcastId: String) {
        if (latestEpisodeId != null) {
            notifyLatestEpisodeIdFetched()
            return
        }
        episodesCache.getAllPodcastEpisodes(
            podcastId,
            object : EpisodesCache.GetAllPodcastEpisodesListener {
                override fun onGetAllEpisodesSuccess(podcastEpisodes: List<Episode>) {
                    latestEpisodeId =
                        if (podcastEpisodes.isNotEmpty()) podcastEpisodes[0].episodeId else null
                    notifyLatestEpisodeIdFetched()
                }

                override fun onGetAllEpisodesFailed() {
                    notifyLatestEpisodeIdFetchFailed()
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

    private fun notifyIsLastPlayedPodcastFetched(isLastPlayedPodcast: Boolean) {
        for (listener in listeners) {
            listener.onIsLastPlayedPodcastFetched(isLastPlayedPodcast)
        }
    }

    private fun notifyIsLastPlayedPodcastFetchFailed() {
        for (listener in listeners) {
            listener.onIsLastPlayedPodcastFetchFailed()
        }
    }

    private fun notifyLatestEpisodeIdFetched() {
        for (listener in listeners) {
            listener.onLatestEpisodeIdFetched(latestEpisodeId)
        }
    }

    private fun notifyLatestEpisodeIdFetchFailed() {
        for (listener in listeners) {
            listener.onLatestEpisodeIdFetchFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}