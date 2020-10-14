package com.atmko.skiptoit.model.database

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.AppExecutors

open class SubscriptionsCache(
    private val subscriptionsDao: SubscriptionsDao?,
    private val prefs: SharedPreferences?
) {

    companion object {
        const val SUBSCRIPTIONS_CACHE_KEY = "subscriptions_cache"
        const val IS_SUBSCRIPTIONS_SYNCED_KEY = "is_subscriptions_synced"
    }

    interface SubscriptionUpdateListener {
        fun onSubscriptionUpdateSuccess()
        fun onSubscriptionUpdateFailed()
    }

    interface SubscriptionStatusListener {
        fun onGetSubscriptionStatusSuccess(subscriptionStatus: Boolean)
        fun onGetSubscriptionStatusFailed()
    }

    interface SyncStatusUpdateListener {
        fun onSyncStatusUpdated()
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

    @SuppressLint("ApplySharedPref")
    open fun setSubscriptionsSynced(isSubscriptionsSynced: Boolean, listener: SyncStatusUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            prefs!!.edit().putBoolean(IS_SUBSCRIPTIONS_SYNCED_KEY, isSubscriptionsSynced).commit()

            AppExecutors.getInstance().mainThread().execute {
                listener.onSyncStatusUpdated()
            }
        }
    }

    open fun isSubscriptionsSynced(listener: SyncStatusFetchListener) {
        AppExecutors.getInstance().diskIO().execute {
            val isSubscriptionsSynced = prefs!!.getBoolean(IS_SUBSCRIPTIONS_SYNCED_KEY, false)

            AppExecutors.getInstance().mainThread().execute {
                listener.onSyncStatusFetched(isSubscriptionsSynced)
            }
        }
    }
}