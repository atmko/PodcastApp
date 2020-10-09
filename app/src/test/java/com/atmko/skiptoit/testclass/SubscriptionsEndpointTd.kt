package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint
import com.atmko.skiptoit.testdata.SubscriptionMocks

class SubscriptionsEndpointTd : SubscriptionsEndpoint(null, null) {

    var mUpdateSubscriptionCounter = 0
    var mPodcastId = ""
    var mSubscriptionStatus: Int? = null
    var mFailure = false
    override fun updateSubscription(podcastId: String, subscriptionStatus: Int, listener: UpdateSubscriptionListener) {
        mUpdateSubscriptionCounter += 1
        mPodcastId = podcastId
        mSubscriptionStatus = subscriptionStatus
        if (!mFailure) {
            listener.onSubscriptionStatusUpdated()
        } else {
            listener.onSubscriptionStatusUpdateFailed()
        }
    }

    var mGetSubscriptionsCounter = 0
    var mGetSubscriptionsFailure = false
    override fun getSubscriptions(listener: RetrieveSubscriptionsListener) {
        mGetSubscriptionsCounter += 1
        if (!mGetSubscriptionsFailure) {
            listener.onSubscriptionsFetchSuccess(SubscriptionMocks.GET_SUBSCRIPTIONS())
        } else {
            listener.onSubscriptionsFetchFailed()
        }
    }
}
