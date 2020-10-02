package com.atmko.skiptoit.subcriptions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toFlowable
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import retrofit2.Response

const val STATUS_SUBSCRIBE = 1
const val STATUS_UNSUBSCRIBE = 0

class SubscriptionsViewModel(private val skipToItApi: SkipToItApi,
                             private val subscriptionsDao: SubscriptionsDao,
                             private val googleSignInClient: GoogleSignInClient) : ViewModel() {

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

    fun unsubscribe(podcastId: String) {
        unsubscribeFromRemoteDatabase(podcastId)
    }

    private fun unsubscribeFromRemoteDatabase(podcastId: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItApi.subscribeOrUnsubscribe(podcastId, it,
                        STATUS_UNSUBSCRIBE
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    unsubscribeFromLocalDatabase(podcastId)
                                } else {
                                    loadError.value = false
                                    loading.value = false
                                }
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                            }
                        })
                )
            }
        }
    }

    private fun unsubscribeFromLocalDatabase(podcastId: String) {
        AppExecutors.getInstance().diskIO().execute(Runnable {
            subscriptionsDao.deleteSubscription(podcastId)

            AppExecutors.getInstance().mainThread().execute(Runnable {
                getSubscriptions()
            })
        })
    }

    override fun onCleared() {
        disposable.clear()
    }
}