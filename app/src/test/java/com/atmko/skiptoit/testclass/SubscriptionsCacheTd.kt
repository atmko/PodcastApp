package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.database.SubscriptionsCache

class  SubscriptionsCacheTd : SubscriptionsCache(null) {

    var mFailure = false
    var mPodcastId = ""

    var mInsertSubscriptionCounter = 0
    lateinit var mPodcast: Podcast
    override fun insertSubscription(podcast: Podcast, listener: SubscriptionUpdateListener) {
        mInsertSubscriptionCounter += 1
        mPodcast = podcast
        if (!mFailure) {
            listener.onSubscriptionUpdateSuccess()
        } else {
            listener.onSubscriptionUpdateFailed()
        }
    }

    var mRemoveSubscriptionCounter = 0
    override fun removeSubscription(podcastId: String, listener: SubscriptionUpdateListener) {
        mRemoveSubscriptionCounter += 1
        mPodcastId = podcastId
        if (!mFailure) {
            listener.onSubscriptionUpdateSuccess()
        } else {
            listener.onSubscriptionUpdateFailed()
        }
    }

    var mGetSubscriptionStatusCounter = 0
    override fun getSubscriptionStatus(podcastId: String, listener: SubscriptionStatusListener) {
        mGetSubscriptionStatusCounter += 1
        mPodcastId = podcastId
        if (!mFailure) {
            listener.onGetSubscriptionStatusSuccess(true)
        } else {
            listener.onGetSubscriptionStatusFailed()
        }
    }
}
