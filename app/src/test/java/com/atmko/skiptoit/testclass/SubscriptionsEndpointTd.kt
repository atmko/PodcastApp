package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint
import com.atmko.skiptoit.testdata.SubscriptionMocks

class SubscriptionsEndpointTd : SubscriptionsEndpoint(null, null) {

    var mUpdateSubscriptionCounter = 0
    var mPodcastId = ""
    var mSubscriptionStatus: Int? = null
    var mUpdateServerSubscriptionFailure = false
    override fun updateSubscription(podcastId: String, subscriptionStatus: Int, listener: UpdateSubscriptionListener) {
        mUpdateSubscriptionCounter += 1
        mPodcastId = podcastId
        mSubscriptionStatus = subscriptionStatus
        if (!mUpdateServerSubscriptionFailure) {
            listener.onSubscriptionStatusUpdated()
        } else {
            listener.onSubscriptionStatusUpdateFailed()
        }
    }

    var mGetSubscriptionsCounter = 0
    var mGetSubscriptionsFailure = false
    var mNoPullablePodcasts = true
    override fun getSubscriptions(listener: RetrieveSubscriptionsListener) {
        mGetSubscriptionsCounter += 1
        if (!mGetSubscriptionsFailure) {
            if (mNoPullablePodcasts) {
                listener.onSubscriptionsFetchSuccess(listOf())
            } else {
                listener.onSubscriptionsFetchSuccess(SubscriptionMocks.GET_SUBSCRIPTIONS())
            }
        } else {
            listener.onSubscriptionsFetchFailed()
        }
    }

    var mBatchSubscribeCounter = 0
    var mBatchSubscribePodcastsFailure = false
    lateinit var mBatchSubscribeArgCombinedPocastIds:String
    override fun batchSubscribe(combinedPodcastIds: String, listener: UpdateSubscriptionListener) {
        mBatchSubscribeCounter += 1
        mBatchSubscribeArgCombinedPocastIds = combinedPodcastIds
        if (!mBatchSubscribePodcastsFailure) {
            listener.onSubscriptionStatusUpdated()
        } else {
            listener.onSubscriptionStatusUpdateFailed()
        }
    }
}
