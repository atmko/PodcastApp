package com.atmko.skiptoit.details

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.PodcastDetails

class DetailsViewModel(
    private val podcastDetailsEndpoint: PodcastDetailsEndpoint
) : BaseViewModel<DetailsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onDetailsFetched(podcastDetails: PodcastDetails)
        fun onDetailsFetchFailed()
    }

    lateinit var podcastDetails: PodcastDetails

    var isSubscribed: Boolean? = null

    fun getDetailsAndNotify(podcastId: String) {
        if (this::podcastDetails.isInitialized) {
            notifyDetailsFetched(podcastDetails)
            return
        }

        notifyProcessing()

        podcastDetailsEndpoint.getPodcastDetails(podcastId, object : PodcastDetailsEndpoint.Listener {
            override fun onPodcastDetailsFetchSuccess(fetchedPodcastDetails: PodcastDetails) {
                podcastDetails = fetchedPodcastDetails
                notifyDetailsFetched(fetchedPodcastDetails)
            }

            override fun onPodcastDetailsFetchFailed() {
                notifyDetailsFetchFailed()
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

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}