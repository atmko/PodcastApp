package com.atmko.skiptoit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

const val STATUS_SUBSCRIBE = 1
const val STATUS_UNSUBSCRIBE = 0

class SubscriptionsViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var skipToItService: SkipToItApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(getApplication())
    }

    val subscriptions: MutableLiveData<List<Podcast>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getSubscriptions() {
        disposable.add(
            SkipToItDatabase.getInstance(getApplication()).subscriptionsDao()
                .getAllSubscriptions()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<Podcast>>() {
                    override fun onSuccess(podcasts: List<Podcast>) {
                        subscriptions.value = podcasts
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadError.value = true
                        loading.value = false
                    }
                })
        )
    }

    fun unsubscribe(podcastId: String) {
        unsubscribeFromRemoteDatabase(podcastId)
    }

    private fun unsubscribeFromRemoteDatabase(podcastId: String) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItService.subscribeOrUnsubscribe(podcastId, it, STATUS_UNSUBSCRIBE)
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
            SkipToItDatabase.getInstance(getApplication()).subscriptionsDao()
                .deleteSubscription(podcastId)

            AppExecutors.getInstance().mainThread().execute(Runnable {
                getSubscriptions()
            })
        })
    }

    override fun onCleared() {
        disposable.clear()
    }
}