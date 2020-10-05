package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint

class SubscriptionsEndpointTd : SubscriptionsEndpoint(null, null) {

    var mUpdateSubscriptionCounter = 0
    var mPodcastId = ""
    var mSubscriptionStatus: Int? = null
    var mFailure = false
    override fun updateSubscription(podcastId: String, subscriptionStatus: Int, listener: Listener) {
        mUpdateSubscriptionCounter += 1
        mPodcastId = podcastId
        mSubscriptionStatus = subscriptionStatus
        if (!mFailure) {
            listener.onSubscriptionStatusUpdated()
        } else {
            listener.onSubscriptionStatusUpdateFailed()
        }
    }
}
