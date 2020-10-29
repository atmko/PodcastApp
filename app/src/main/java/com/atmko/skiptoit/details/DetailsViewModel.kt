package com.atmko.skiptoit.details

import android.util.Log
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.subcriptions.STATUS_SUBSCRIBE
import com.atmko.skiptoit.subcriptions.STATUS_UNSUBSCRIBE
import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint

class DetailsViewModel(
    private val podcastDetailsEndpoint: PodcastDetailsEndpoint,
    private val subscriptionsEndpoint: SubscriptionsEndpoint,
    private val subscriptionsCache: SubscriptionsCache
) : BaseViewModel<DetailsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onDetailsFetched(podcastDetails: PodcastDetails)
        fun onDetailsFetchFailed()
        fun onStatusUpdated(isSubscribed: Boolean)
        fun onStatusUpdateFailed()
        fun onStatusFetched(isSubscribed: Boolean)
        fun onStatusFetchFailed()
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

    fun loadSubscriptionStatusAndNotify(podcastId: String) {
        if (isSubscribed != null) {
            notifyStatusFetched(isSubscribed!!)
            return
        }

        notifyProcessing()

        subscriptionsCache.getSubscriptionStatus(podcastId, object : SubscriptionsCache.SubscriptionStatusListener {
            override fun onGetSubscriptionStatusSuccess(subscriptionStatus: Boolean) {
                isSubscribed = subscriptionStatus
                notifyStatusFetched(subscriptionStatus)
            }

            override fun onGetSubscriptionStatusFailed() {
                notifyStatusFetchFailed()
            }
        })
    }

    //toggle remote subscriptions status then toggle local subscription status
    fun toggleSubscriptionAndNotify(podcast: Podcast) {
        if (isSubscribed == null) return
        notifyProcessing()
        val subscribeStatus = if (isSubscribed!!) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        subscriptionsEndpoint.updateSubscription(podcast.id, subscribeStatus,
            object : SubscriptionsEndpoint.UpdateSubscriptionListener {
                override fun onSubscriptionStatusUpdated() {
                    toggleLocalSubscriptionAndNotify(podcast)
                }

                override fun onSubscriptionStatusUpdateFailed() {
                    notifyStatusUpdateFailed()
                }
            }
        )
    }

    //toggle local subscription status
    fun toggleLocalSubscriptionAndNotify(podcast: Podcast) {
        if (isSubscribed == null) return
        val subscribeStatus = if (isSubscribed!!) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        if (subscribeStatus == STATUS_SUBSCRIBE) {
            subscriptionsCache.insertSubscription(listOf(podcast), object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    isSubscribed = true
                    notifyStatusUpdated(isSubscribed!!)
                }

                override fun onSubscriptionUpdateFailed() {
                    notifyStatusUpdateFailed()
                }
            })
        } else {
            subscriptionsCache.removeSubscription(podcast.id, object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    isSubscribed = false
                    notifyStatusUpdated(isSubscribed!!)
                }

                override fun onSubscriptionUpdateFailed() {
                    notifyStatusUpdateFailed()
                }
            })
        }
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

    private fun notifyStatusUpdated(isSubscribed: Boolean) {
        for (listener in listeners) {
            listener.onStatusUpdated(isSubscribed)
        }
    }

    private fun notifyStatusUpdateFailed() {
        for (listener in listeners) {
            listener.onStatusUpdateFailed()
        }
    }

    private fun notifyStatusFetched(subscriptionStatus: Boolean) {
        for (listener in listeners) {
            listener.onStatusFetched(subscriptionStatus)
        }
    }

    private fun notifyStatusFetchFailed() {
        for (listener in listeners) {
            listener.onStatusFetchFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}