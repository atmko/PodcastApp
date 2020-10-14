package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.database.SubscriptionsCache

class  SubscriptionsCacheTd : SubscriptionsCache(null, null) {

    var mFailure = false
    var mPodcastId = ""

    var mInsertSubscriptionCounter = 0
    lateinit var mPodcasts: List<Podcast>
    override fun insertSubscription(podcasts: List<Podcast>, listener: SubscriptionUpdateListener) {
        mInsertSubscriptionCounter += 1
        mPodcasts = podcasts
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

    var mSetSubscriptionsSyncedCounter = 0
    var mIsSubscriptionsSynced: Boolean = true
    override fun setSubscriptionsSynced(isSubscriptionsSynced: Boolean, listener: SyncStatusUpdateListener) {
        mSetSubscriptionsSyncedCounter += 1
        mIsSubscriptionsSynced = isSubscriptionsSynced
        listener.onSyncStatusUpdated()
    }

    var mIsSubscriptionsSyncedCounter = 0
    var mIsUserLoggedIn: Boolean = true
    override fun isSubscriptionsSynced(listener: SyncStatusFetchListener) {
        mIsSubscriptionsSyncedCounter += 1
        listener.onSyncStatusFetched(mIsSubscriptionsSynced)
    }
}
