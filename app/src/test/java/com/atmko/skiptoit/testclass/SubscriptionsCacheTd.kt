package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.testdata.PodcastMocks

class  SubscriptionsCacheTd : SubscriptionsCache(null, null) {

    var mFailure = false
    var mPodcastId = ""

    var mInsertSubscriptionCounter = 0
    lateinit var mInsertSubscriptionArgPodcasts: List<Podcast>
    override fun insertSubscription(podcasts: List<Podcast>, listener: SubscriptionUpdateListener) {
        mInsertSubscriptionCounter += 1
        mInsertSubscriptionArgPodcasts = podcasts
        if (!mFailure) {
            listener.onSubscriptionUpdateSuccess()
        } else {
            listener.onSubscriptionUpdateFailed()
        }
    }

    var mGetSubscriptionsForSyncCounter = 0
    var mGetSubscriptionsForSyncFailure = false
    var mNoPushablePodcasts = true
    override fun getSubscriptionsForSync(listener: FetchSubscriptionsListener) {
        mGetSubscriptionsForSyncCounter += 1
        if (!mGetSubscriptionsForSyncFailure) {
            if (mNoPushablePodcasts) {
                listener.onFetchSubscriptionsSuccess(listOf())
            } else {
                listener.onFetchSubscriptionsSuccess(listOf(PodcastMocks.GET_PODCAST_3(), PodcastMocks.GET_PODCAST_4()))
            }
        } else {
            listener.onFetchSubscriptionFailed()
        }
    }

    var mGetSubscriptionsCounter = 0
    var mGetSubscriptionsFailure = false
    override fun getSubscriptions(listener: FetchSubscriptionsListener) {
        mGetSubscriptionsCounter += 1
        if (!mGetSubscriptionsFailure) {
            listener.onFetchSubscriptionsSuccess(listOf(PodcastMocks.GET_PODCAST_1(), PodcastMocks.GET_PODCAST_2()))
        } else {
            listener.onFetchSubscriptionFailed()
        }
    }

    var mGetSubscriptionsLiveDataCounter = 0
    override fun getSubscriptionsLiveData(listener: FetchSubscriptionsLiveDataListener) {
        mGetSubscriptionsLiveDataCounter++
        listener.onFetchSubscriptionsSuccess(PodcastMocks.PodcastLiveDataMocks.GET_PODCAST_LIST())
    }

    var mRemoveSubscriptionCounter = 0
    var mRemoveSubscriptionFailure = false
    override fun removeSubscription(podcastId: String, listener: SubscriptionUpdateListener) {
        mRemoveSubscriptionCounter += 1
        mPodcastId = podcastId
        if (!mRemoveSubscriptionFailure) {
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
