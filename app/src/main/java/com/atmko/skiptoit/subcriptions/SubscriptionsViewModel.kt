package com.atmko.skiptoit.subcriptions

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toFlowable
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Subscription
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
        fun onSubscriptionsSyncStatusSynced()
        fun onSubscriptionsSyncStatusSyncFailed()
        fun onStatusUpdated()
        fun onStatusUpdateFailed()
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    //state save variables
    var scrollPosition: Int = 0

    var mIsSubscriptionsSynced: Boolean? = null

    val subscriptions: MutableLiveData<PagedList<Podcast>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

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
        loginManager.silentSignIn(object : LoginManager.SignInListener {
            override fun onSignInSuccess(googleSignInAccount: GoogleSignInAccount) {
                loginManager.isSubscriptionsSynced(object : SubscriptionsCache.SyncStatusFetchListener {
                    override fun onSyncStatusFetched(isSubscriptionsSynced: Boolean) {
                        if (!isSubscriptionsSynced) {
                            restoreSubscriptionsAndNotify()
                        } else {
                            mIsSubscriptionsSynced = isSubscriptionsSynced
                            notifyOnSubscriptionStatusSynced()
                        }
                    }
                })
            }

            override fun onSignInFailed(googleSignInIntent: Intent) {

            }
        })

    }

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
        subscriptionsEndpoint.getSubscriptions(object : SubscriptionsEndpoint.RetrieveSubscriptionsListener {
            override fun onSubscriptionsFetchSuccess(subscriptions: List<Subscription>) {
                getBatchPodcastData(subscriptions)
            }

            override fun onSubscriptionsFetchFailed() {
                setSubscriptionsSynced(false)
            }
        })
    }

    private fun getBatchPodcastData(subscriptions: List<Subscription>) {
        podcastsEndpoint.getBatchPodcastMetadata(combinePodcastIds(subscriptions), object : PodcastsEndpoint.BatchFetchPodcastsListener {
            override fun onBatchFetchSuccess(apiResults: ApiResults) {
                saveToLocalDatabase(apiResults.podcasts)
            }

            override fun onBatchFetchFailed() {
                setSubscriptionsSynced(false)
            }
        })
    }

    private fun saveToLocalDatabase(podcasts: List<Podcast>) {
        subscriptionsCache.insertSubscription(podcasts, object : SubscriptionsCache.SubscriptionUpdateListener {
            override fun onSubscriptionUpdateSuccess() {
                setSubscriptionsSynced(true)
            }

            override fun onSubscriptionUpdateFailed() {
                setSubscriptionsSynced(false)
            }
        })
    }

    private fun setSubscriptionsSynced(isSubscriptionsSynced: Boolean) {
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