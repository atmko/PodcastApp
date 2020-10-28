package com.atmko.skiptoit.model.database

import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.AppExecutors

open class SubscriptionsCache(
    private val subscriptionsDao: SubscriptionsDao?
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

    interface SubscriptionStatusListener {
        fun onGetSubscriptionStatusSuccess(subscriptionStatus: Boolean)
        fun onGetSubscriptionStatusFailed()
    }

    interface SyncStatusFetchListener {
        fun onSyncStatusFetched(isSubscriptionsSynced: Boolean)
    }

    open fun insertSubscription(podcasts: List<Podcast>, listener: SubscriptionUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            subscriptionsDao!!.createSubscription(podcasts)

            AppExecutors.getInstance().mainThread().execute {
                listener.onSubscriptionUpdateSuccess()
            }
        }
    }

    open fun getSubscriptions(listener: FetchSubscriptionsListener) {
        AppExecutors.getInstance().diskIO().execute {
            val localSubscriptions = subscriptionsDao!!.getAllSubscriptionsAlt()
            AppExecutors.getInstance().mainThread().execute {
                listener.onFetchSubscriptionsSuccess(localSubscriptions)
            }
        }
    }

    open fun removeSubscription(podcastId: String, listener: SubscriptionUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            subscriptionsDao!!.deleteSubscription(podcastId)

            AppExecutors.getInstance().mainThread().execute {
                listener.onSubscriptionUpdateSuccess()
            }
        }
    }

    open fun getSubscriptionStatus(podcastId: String, listener: SubscriptionStatusListener) {
        AppExecutors.getInstance().diskIO().execute {
            val isSubscribed = subscriptionsDao!!.isSubscribed(podcastId)

            AppExecutors.getInstance().mainThread().execute {
                listener.onGetSubscriptionStatusSuccess(isSubscribed)
            }
        }
    }
}