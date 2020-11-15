package com.atmko.skiptoit.model.database

import androidx.lifecycle.LiveData
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.AppExecutors

open class SubscriptionsCache(
    private val subscriptionsDao: SubscriptionsDao?,
    private val appExecutors: AppExecutors
) {

    companion object {
        const val SUBSCRIPTIONS_CACHE_KEY = "subscriptions_cache"
        const val IS_SUBSCRIPTIONS_SYNCED_KEY = "is_subscriptions_synced"
    }

    interface SubscriptionUpdateListener {
        fun onSubscriptionUpdateSuccess()
        fun onSubscriptionUpdateFailed()
    }

    interface FetchSubscriptionsListener {
        fun onFetchSubscriptionsSuccess(localSubscriptions: List<Podcast>)
        fun onFetchSubscriptionFailed()
    }

    interface FetchSubscriptionsLiveDataListener {
        fun onFetchSubscriptionsSuccess(localSubscriptions: LiveData<List<Podcast>>)
    }

    interface SubscriptionStatusListener {
        fun onGetSubscriptionStatusSuccess(subscriptionStatus: Boolean)
        fun onGetSubscriptionStatusFailed()
    }

    interface SyncStatusFetchListener {
        fun onSyncStatusFetched(isSubscriptionsSynced: Boolean)
    }

    open fun insertSubscription(podcasts: List<Podcast>, listener: SubscriptionUpdateListener) {
        appExecutors.diskIO.execute {
            subscriptionsDao!!.createSubscription(podcasts)

            appExecutors.mainThread.execute {
                listener.onSubscriptionUpdateSuccess()
            }
        }
    }

    open fun getSubscriptionsForSync(listener: FetchSubscriptionsListener) {
        appExecutors.diskIO.execute {
            val localSubscriptions = subscriptionsDao!!.getAllSubscriptionsAlt()
            appExecutors.mainThread.execute {
                listener.onFetchSubscriptionsSuccess(localSubscriptions)
            }
        }
    }

    open fun getSubscriptions(listener: FetchSubscriptionsListener) {
        appExecutors.diskIO.execute {
            val localSubscriptions = subscriptionsDao!!.getAllSubscriptionsAlt()
            appExecutors.mainThread.execute {
                listener.onFetchSubscriptionsSuccess(localSubscriptions)
            }
        }
    }

    open fun getSubscriptionsLiveData(listener: FetchSubscriptionsLiveDataListener) {
        listener.onFetchSubscriptionsSuccess(subscriptionsDao!!.getAllSubscriptions())
    }

    open fun removeSubscription(podcastId: String, listener: SubscriptionUpdateListener) {
        appExecutors.diskIO.execute {
            subscriptionsDao!!.deleteSubscription(podcastId)

            appExecutors.mainThread.execute {
                listener.onSubscriptionUpdateSuccess()
            }
        }
    }

    open fun getSubscriptionStatus(podcastId: String, listener: SubscriptionStatusListener) {
        appExecutors.diskIO.execute {
            val isSubscribed = subscriptionsDao!!.isSubscribed(podcastId)

            appExecutors.mainThread.execute {
                listener.onGetSubscriptionStatusSuccess(isSubscribed)
            }
        }
    }
}