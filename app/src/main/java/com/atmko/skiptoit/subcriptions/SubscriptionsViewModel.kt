package com.atmko.skiptoit.subcriptions

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toFlowable
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.Subscription
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

const val STATUS_SUBSCRIBE = 1
const val STATUS_UNSUBSCRIBE = 0

class SubscriptionsViewModel(
    private val loginManager: LoginManager,
    private var podcastsEndpoint: PodcastsEndpoint,
    private val subscriptionsEndpoint: SubscriptionsEndpoint,
    private val subscriptionsCache: SubscriptionsCache,
    private val subscriptionsDao: SubscriptionsDao
) : BaseViewModel<SubscriptionsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onSilentSignInSuccess()
        fun onSilentSignInFailed()
        fun onSubscriptionsSyncStatusSynced()
        fun onSubscriptionsSyncStatusSyncFailed()
        fun onStatusUpdated()
        fun onStatusUpdateFailed()
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    //state save variables
    var scrollPosition: Int = 0

    var mIsRemoteSubscriptionsSynced: Boolean? = null
    var mIsLocalSubscriptionsSynced: Boolean? = null
    var mIsSubscriptionsSynced: Boolean? = null

    val subscriptions: MutableLiveData<PagedList<Podcast>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    // todo: not yet tested
    val subscriptionsMap: HashMap<String, Unit?> = HashMap()

    fun silentSignIn() {
        loginManager.silentSignIn(object : LoginManager.SignInListener {
            override fun onSignInSuccess(googleSignInAccount: GoogleSignInAccount) {
                notifySilentSignInSuccess()
            }

            override fun onSignInFailed(googleSignInIntent: Intent) {
                notifySilentSignInFailed()
            }
        })
    }

    fun checkSyncStatusAndNotify() {
        if (mIsSubscriptionsSynced != null) {
            if (mIsSubscriptionsSynced!!) {
                notifyOnSubscriptionStatusSynced()
            } else {
                notifyOnSubscriptionStatusSyncFailed()
            }
            return
        }

        notifyProcessing()
        loginManager.isSubscriptionsSynced(object : SubscriptionsCache.SyncStatusFetchListener {
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

    // todo: not yet tested
    fun getSubscriptions() {
        if (subscriptions.value != null) {
            return
        }
        disposable.add(
            subscriptionsDao
                .getAllSubscriptions().toFlowable(20, 1)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSubscriber<PagedList<Podcast>>() {
                    override fun onError(e: Throwable) {
                        loadError.value = true
                        loading.value = false
                    }

                    override fun onComplete() {

                    }

                    override fun onNext(podcasts: PagedList<Podcast>) {
                        for (podcast in podcasts.toList()) {
                            subscriptionsMap[podcast.id] = null
                        }
                        subscriptions.value = podcasts
                        loadError.value = false
                        loading.value = false
                    }
                })
        )
    }

    //remove subscription from remote and then remove subscription locally
    fun unsubscribeAndNotify(podcastId: String) {
        notifyProcessing()
        subscriptionsEndpoint.updateSubscription(podcastId, STATUS_UNSUBSCRIBE, object : SubscriptionsEndpoint.UpdateSubscriptionListener {
            override fun onSubscriptionStatusUpdated() {
                unsubscribeLocallyAndNotify(podcastId)
            }

            override fun onSubscriptionStatusUpdateFailed() {
                notifyStatusUpdateFailed()
            }
        })
    }

    //remove subscription locally only
    fun unsubscribeLocallyAndNotify(podcastId: String) {
        subscriptionsCache.removeSubscription(
            podcastId,
            object : SubscriptionsCache.SubscriptionUpdateListener {
                override fun onSubscriptionUpdateSuccess() {
                    subscriptionsMap.remove(podcastId)
                    notifyStatusUpdated()
                }

                override fun onSubscriptionUpdateFailed() {
                    notifyStatusUpdateFailed()
                }
            })
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
        subscriptionsEndpoint.getSubscriptions(object : SubscriptionsEndpoint.RetrieveSubscriptionsListener {
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
        subscriptionsCache.getSubscriptions(object : SubscriptionsCache.FetchSubscriptionsListener {
            override fun onFetchSubscriptionsSuccess(localSubscriptions: List<Podcast>) {
                val serverSubscriptionsMap = createServerSubscriptionsMap(serverSubscriptions)
                val localSubscriptionsMap = createLocalSubscriptionsMap(localSubscriptions)

                val pushablePodcasts = getExcludedSubscriptions(serverSubscriptionsMap, localSubscriptionsMap.values)
                val pullablePodcasts = getExcludedSubscriptions(localSubscriptionsMap, serverSubscriptionsMap.values)

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
        podcastsEndpoint.getBatchPodcastMetadata(combinePodcastIds(subscriptions), object : PodcastsEndpoint.BatchFetchPodcastsListener {
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
        subscriptionsCache.insertSubscription(podcasts, object : SubscriptionsCache.SubscriptionUpdateListener {
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
            val isSubscriptionsSynced = mIsRemoteSubscriptionsSynced!! && mIsLocalSubscriptionsSynced!!
            loginManager.setSubscriptionsSynced(isSubscriptionsSynced, object : LoginManager.SyncStatusUpdateListener {
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

    private fun createServerSubscriptionsMap(serverSubscriptions: List<Subscription>) : Map<String, Subscription> {
        val serverSubscriptionsMap = HashMap<String, Subscription>()
        for (subscription in serverSubscriptions) {
            serverSubscriptionsMap[subscription.listenNotesId] = subscription
        }
        return serverSubscriptionsMap
    }

    private fun createLocalSubscriptionsMap(localSubscriptions: List<Podcast>) : Map<String, Subscription> {
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

    private fun notifySilentSignInSuccess() {
        for (listener in listeners) {
            listener.onSilentSignInSuccess()
        }
    }

    private fun notifySilentSignInFailed() {
        for (listener in listeners) {
            listener.onSilentSignInFailed()
        }
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

    private fun notifyStatusUpdated() {
        for (listener in listeners) {
            listener.onStatusUpdated()
        }
    }

    private fun notifyStatusUpdateFailed() {
        for (listener in listeners) {
            listener.onStatusUpdateFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}