package com.atmko.skiptoit.subcriptions

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toFlowable
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.common.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

const val STATUS_SUBSCRIBE = 1
const val STATUS_UNSUBSCRIBE = 0

class SubscriptionsViewModel(
    private val subscriptionsEndpoint: SubscriptionsEndpoint,
    private val subscriptionsCache: SubscriptionsCache,
    private val subscriptionsDao: SubscriptionsDao
) : BaseViewModel<SubscriptionsViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onStatusUpdated()
        fun onStatusUpdateFailed()
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    //state save variables
    var scrollPosition: Int = 0

    val subscriptions: MutableLiveData<PagedList<Podcast>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

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

    fun unsubscribeAndNotify(podcastId: String) {
        notifyProcessing()
        subscriptionsEndpoint.updateSubscription(podcastId, STATUS_UNSUBSCRIBE, object : SubscriptionsEndpoint.UpdateSubscriptionListener {
            override fun onSubscriptionStatusUpdated() {
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

            override fun onSubscriptionStatusUpdateFailed() {
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