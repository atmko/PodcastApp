package com.atmko.skiptoit.model.database

import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.AppExecutors

open class SubscriptionsCache(
    private val subscriptionsDao: SubscriptionsDao?
) {

    interface SubscriptionUpdateListener {
        fun onSubscriptionUpdateSuccess()
        fun onSubscriptionUpdateFailed()
    }

    interface SubscriptionStatusListener {
        fun onGetSubscriptionStatusSuccess(subscriptionStatus: Boolean)
        fun onGetSubscriptionStatusFailed()
    }

    open fun insertSubscription(podcast: Podcast, listener: SubscriptionUpdateListener) {
        AppExecutors.getInstance().diskIO().execute {
            subscriptionsDao!!.createSubscription(podcast)

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
}