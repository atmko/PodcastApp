package com.atmko.skiptoit.subcriptions

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.Subscription
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

const val STATUS_SUBSCRIBE = 1
const val STATUS_UNSUBSCRIBE = 0

class SubscriptionsViewModel(
    private val loginManager: LoginManager,
    private var podcastsEndpoint: PodcastsEndpoint,
    private val subscriptionsEndpoint: SubscriptionsEndpoint,
    private val subscriptionsCache: SubscriptionsCache
) : BaseViewModel<SubscriptionsViewModel.Listener>() {

    init {
        // initialize fetch of subscriptions live data
        getSubscriptionsLiveData()
        // initialize fetch of subscriptions and save to subscriptions map
        getSubscriptions()
    }

    interface Listener {
        fun notifyProcessing()
        fun onSubscriptionsSyncStatusSynced()
        fun onSubscriptionsSyncStatusSyncFailed()
    }

    private val toggleListeners = mutableListOf<ToggleSubscriptionListener>()

    interface ToggleSubscriptionListener {
        fun notifySubscriptionToggleProcessing()
        fun onSubscriptionToggleSuccess(isSubscribed: Boolean)
        fun onSubscriptionToggleFailed()
    }

    fun registerToggleListener(listener: ToggleSubscriptionListener) {
        toggleListeners.add(listener)
    }

    fun unregisterToggleListener(listener: ToggleSubscriptionListener) {
        toggleListeners.remove(listener)
    }

    private fun unregisterToggleListeners() {
        for (listener in toggleListeners) {
            unregisterToggleListener(listener)
        }
    }

    private val subscriptionStatusListeners = mutableListOf<FetchSubscriptionStatusListener>()

    interface FetchSubscriptionStatusListener {
        fun notifyFetchingSubscriptionStatus()
        fun onSubscriptionStatusFetched(isSubscribed: Boolean)
        fun onSubscriptionStatusFetchFailed()
    }

    fun registerSubscriptionStatusListener(listener: FetchSubscriptionStatusListener) {
        subscriptionStatusListeners.add(listener)
    }

    fun unregisterSubscriptionStatusListener(listener: FetchSubscriptionStatusListener) {
        subscriptionStatusListeners.remove(listener)
    }

    private fun unregisterSubscriptionStatusListeners() {
        for (listener in subscriptionStatusListeners) {
            unregisterSubscriptionStatusListener(listener)
        }
    }

    //state save variables
    var scrollPosition: Int = 0

    var mIsRemoteSubscriptionsSynced: Boolean? = null
    var mIsLocalSubscriptionsSynced: Boolean? = null
    var mIsSubscriptionsSynced: Boolean? = null

    var subscriptionsLiveData: LiveData<List<Podcast>>? = null

    // todo: not yet tested
    var subscriptionsMap: HashMap<String, Unit?>? = null

    fun checkSyncStatusAndNotify() {
        if (mIsSubscriptionsSynced != null) {
            if (mIsSubscriptionsSynced!!) {
                notifyOnSubscriptionStatusSynced()
            } else {
                notifyOnSubscriptionStatusSyncFailed()
            }
            return
        }

        loginManager.silentSignIn(object : LoginManager.SignInListener {
            override fun onSignInSuccess(googleSignInAccount: GoogleSignInAccount) {
                notifyProcessing()
                loginManager.isSubscriptionsSynced(object :
                    SubscriptionsCache.SyncStatusFetchListener {
                    override fun onSyncStatusFetched(isSubscriptionsSynced: Boolean) {
                        if (!isSubscriptionsSynced) {
                            restoreSubscriptionsAndNotify()
                        } else {
                            mIsRemoteSubscriptionsSynced = true
                            mIsLocalSubscriptionsSynced = true
                            mIsSubscriptionsSynced = isSubscriptionsSynced
                            notifyOnSubscriptionStatusSynced()
                        }
                    }
                })
            }

            override fun onSignInFailed(googleSignInIntent: Intent) {
                System.out.println("subscriptions sync failed: user not signed in")
            }
        })
    }

    // todo: not yet tested
    // get local subscriptions live data
    private fun getSubscriptionsLiveData() {
        if (subscriptionsLiveData != null) {
            return
        }

        subscriptionsCache.getSubscriptionsLiveData(object :
            SubscriptionsCache.FetchSubscriptionsLiveDataListener {
            override fun onFetchSubscriptionsSuccess(localSubscriptions: LiveData<List<Podcast>>) {
                subscriptionsLiveData = localSubscriptions
            }
        })
    }

    // get subscriptions and save results in subscriptions map
    private fun getSubscriptions() {
        subscriptionsCache.getSubscriptions(object :
            SubscriptionsCache.FetchSubscriptionsListener {
            override fun onFetchSubscriptionsSuccess(localSubscriptions: List<Podcast>) {
                saveSubscriptionMap(localSubscriptions)
            }

            override fun onFetchSubscriptionFailed() {

            }
        })
    }

    fun saveSubscriptionMap(subscriptions: List<Podcast>) {
        subscriptionsMap = HashMap()
        for (podcast in subscriptions) {
            setIsSubscribed(podcast.id, true)//todo: not tested
        }
    }

    //toggle remote subscriptions status then toggle local subscription status
    fun toggleSubscriptionAndNotify(podcast: Podcast) {
        if (subscriptionsMap == null) return
        notifyToggleStatusProcessing()
        val subscribeStatus = if (isSubscribed(podcast.id)) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        subscriptionsEndpoint.updateSubscription(podcast.id, subscribeStatus,
            object : SubscriptionsEndpoint.UpdateSubscriptionListener {
                override fun onSubscriptionStatusUpdated() {
                    toggleLocalSubscriptionAndNotify(podcast)
                }

                override fun onSubscriptionStatusUpdateFailed() {
                    notifySubscriptionToggleFailed()
                }
            }
        )
    }

    //toggle local subscription status
    fun toggleLocalSubscriptionAndNotify(podcast: Podcast) {
        if (subscriptionsMap == null) return
        val subscribeStatus = if (isSubscribed(podcast.id)) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        if (subscribeStatus == STATUS_SUBSCRIBE) {
            subscribeLocallyAndNotify(podcast)
        } else {
            unsubscribeLocallyAndNotify(podcast.id)
        }
    }

    //add subscription locally only
    private fun subscribeLocallyAndNotify(podcast: Podcast) {
        subscriptionsCache.insertSubscription(
            listOf(podcast),
            object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    setIsSubscribed(podcast.id, true)//todo: not tested
                    notifySubscriptionToggleSuccess(true)//todo: not tested
                }

                override fun onSubscriptionUpdateFailed() {
                    notifySubscriptionToggleFailed()
                }
            })
    }

    //remove subscription locally only
    private fun unsubscribeLocallyAndNotify(podcastId: String) {
        subscriptionsCache.removeSubscription(
            podcastId,
            object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    setIsSubscribed(podcastId, false)//todo: not tested
                    notifySubscriptionToggleSuccess(false)//todo: not tested
                }

                override fun onSubscriptionUpdateFailed() {
                    notifySubscriptionToggleFailed()
                }
            })
    }

    fun getSubscriptionStatusAndNotify(podcastId: String) {
        notifySubscriptionStatusProcessing()
        if (subscriptionsMap != null) {
            notifySubscriptionStatusFetched(isSubscribed(podcastId))
        } else {
            notifySubscriptionStausFetchFailed()
        }
    }

    private fun isSubscribed(podcastId: String): Boolean {
        return subscriptionsMap!!.containsKey(podcastId)
    }

    private fun setIsSubscribed(podcastId: String, isSubscribed: Boolean) {
        if (isSubscribed) {
            subscriptionsMap!![podcastId] = null
        } else {
            subscriptionsMap!!.remove(podcastId)
        }
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    private fun notifyProcessing() {
        for (listener in listeners) {
            listener.notifyProcessing()
        }
    }

    fun restoreSubscriptionsAndNotify() {
        mIsRemoteSubscriptionsSynced = null
        mIsLocalSubscriptionsSynced = null
        subscriptionsEndpoint.getSubscriptions(object :
            SubscriptionsEndpoint.RetrieveSubscriptionsListener {
            override fun onSubscriptionsFetchSuccess(serverSubscriptions: List<Subscription>) {
                getLocalSubscriptions(serverSubscriptions)
            }

            override fun onSubscriptionsFetchFailed() {
                mIsRemoteSubscriptionsSynced = false
                mIsLocalSubscriptionsSynced = false
                setSubscriptionsSynced()
            }
        })
    }

    private fun getLocalSubscriptions(serverSubscriptions: List<Subscription>) {
        subscriptionsCache.getSubscriptionsForSync(object :
            SubscriptionsCache.FetchSubscriptionsListener {
            override fun onFetchSubscriptionsSuccess(localSubscriptions: List<Podcast>) {
                val serverSubscriptionsMap = createServerSubscriptionsMap(serverSubscriptions)
                val localSubscriptionsMap = createLocalSubscriptionsMap(localSubscriptions)

                val pushablePodcasts =
                    getExcludedSubscriptions(serverSubscriptionsMap, localSubscriptionsMap.values)
                val pullablePodcasts =
                    getExcludedSubscriptions(localSubscriptionsMap, serverSubscriptionsMap.values)

                if (pushablePodcasts.isNotEmpty()) {
                    batchSubscribePodcasts(pushablePodcasts)
                } else {
                    mIsRemoteSubscriptionsSynced = true
                    setSubscriptionsSynced()
                }

                if (pullablePodcasts.isNotEmpty()) {
                    getBatchPodcastData(pullablePodcasts)
                } else {
                    mIsLocalSubscriptionsSynced = true
                    setSubscriptionsSynced()
                }
            }

            override fun onFetchSubscriptionFailed() {
                mIsRemoteSubscriptionsSynced = false
                mIsLocalSubscriptionsSynced = false
                setSubscriptionsSynced()
            }
        })
    }

    private fun getBatchPodcastData(subscriptions: List<Subscription>) {
        podcastsEndpoint.getBatchPodcastMetadata(
            combinePodcastIds(subscriptions),
            object : PodcastsEndpoint.BatchFetchPodcastsListener {
                override fun onBatchFetchSuccess(apiResults: ApiResults) {
                    saveToLocalDatabase(apiResults.podcasts)
                }

                override fun onBatchFetchFailed() {
                    mIsLocalSubscriptionsSynced = false
                    setSubscriptionsSynced()
                }
            })
    }

    fun batchSubscribePodcasts(subscriptions: List<Subscription>) {
        subscriptionsEndpoint.batchSubscribe(combinePodcastIds(subscriptions),
            object : SubscriptionsEndpoint.UpdateSubscriptionListener {
                override fun onSubscriptionStatusUpdated() {
                    mIsRemoteSubscriptionsSynced = true
                    setSubscriptionsSynced()
                }

                override fun onSubscriptionStatusUpdateFailed() {
                    mIsRemoteSubscriptionsSynced = false
                    setSubscriptionsSynced()
                }
            }
        )
    }

    private fun saveToLocalDatabase(podcasts: List<Podcast>) {
        subscriptionsCache.insertSubscription(
            podcasts,
            object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    mIsLocalSubscriptionsSynced = true
                    setSubscriptionsSynced()
                }

                override fun onSubscriptionUpdateFailed() {
                    mIsLocalSubscriptionsSynced = false
                    setSubscriptionsSynced()
                }
            })
    }

    private fun setSubscriptionsSynced() {
        if (mIsRemoteSubscriptionsSynced != null && mIsLocalSubscriptionsSynced != null) {
            val isSubscriptionsSynced =
                mIsRemoteSubscriptionsSynced!! && mIsLocalSubscriptionsSynced!!
            loginManager.setSubscriptionsSynced(
                isSubscriptionsSynced,
                object : LoginManager.SyncStatusUpdateListener {
                    override fun onSyncStatusUpdated() {
                        mIsSubscriptionsSynced = isSubscriptionsSynced
                        if (isSubscriptionsSynced) {
                            notifyOnSubscriptionStatusSynced()
                        } else {
                            notifyOnSubscriptionStatusSyncFailed()
                        }
                    }
                })
        }
    }

    private fun combinePodcastIds(subscriptions: List<Subscription>): String {
        val builder: StringBuilder = java.lang.StringBuilder()
        var counter = 0
        while (counter < subscriptions.size) {
            builder.append(subscriptions[counter].listenNotesId)
            if (counter != subscriptions.size - 1) {
                builder.append(",")
            }
            counter += 1
        }

        return builder.toString()
    }

    private fun createServerSubscriptionsMap(serverSubscriptions: List<Subscription>): Map<String, Subscription> {
        val serverSubscriptionsMap = HashMap<String, Subscription>()
        for (subscription in serverSubscriptions) {
            serverSubscriptionsMap[subscription.listenNotesId] = subscription
        }
        return serverSubscriptionsMap
    }

    private fun createLocalSubscriptionsMap(localSubscriptions: List<Podcast>): Map<String, Subscription> {
        val localSubscriptionsMap = HashMap<String, Subscription>()
        for (podcast in localSubscriptions) {
            localSubscriptionsMap[podcast.id] = Subscription(podcast.id)
        }
        return localSubscriptionsMap
    }

    private fun getExcludedSubscriptions(
        subscriptionsMap: Map<String, Subscription>,
        checklist: Collection<Subscription>
    ): List<Subscription> {
        val notInMap: ArrayList<Subscription> = arrayListOf()
        for (subscription in checklist) {
            if (!subscriptionsMap.containsKey(subscription.listenNotesId)) {
                notInMap.add(subscription)
            }
        }
        return notInMap
    }

    private fun notifyOnSubscriptionStatusSynced() {
        for (listener in listeners) {
            listener.onSubscriptionsSyncStatusSynced()
        }
    }

    private fun notifyOnSubscriptionStatusSyncFailed() {
        for (listener in listeners) {
            listener.onSubscriptionsSyncStatusSyncFailed()
        }
    }

    private fun notifyToggleStatusProcessing() {
        for (listener in toggleListeners) {
            listener.notifySubscriptionToggleProcessing()
        }
    }

    private fun notifySubscriptionToggleSuccess(isSubscribed: Boolean) {
        for (listener in toggleListeners) {
            listener.onSubscriptionToggleSuccess(isSubscribed)
        }
    }

    private fun notifySubscriptionToggleFailed() {
        for (listener in toggleListeners) {
            listener.onSubscriptionToggleFailed()
        }
    }

    private fun notifySubscriptionStatusProcessing() {
        for (listener in subscriptionStatusListeners) {
            listener.notifyFetchingSubscriptionStatus()
        }
    }

    private fun notifySubscriptionStatusFetched(isSubscribed: Boolean) {
        for (listener in subscriptionStatusListeners) {
            listener.onSubscriptionStatusFetched(isSubscribed)
        }
    }

    private fun notifySubscriptionStausFetchFailed() {
        for (listener in subscriptionStatusListeners) {
            listener.onSubscriptionStatusFetchFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
        unregisterToggleListeners()
        unregisterSubscriptionStatusListeners()
    }
}