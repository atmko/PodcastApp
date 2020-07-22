package com.atmko.skiptoit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.dependencyinjection.DaggerSkipToItApiComponent
import com.atmko.skiptoit.model.SkipToItService
import com.atmko.skiptoit.model.Subscription
import com.atmko.skiptoit.util.toBoolean
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
    lateinit var skipToItService: SkipToItService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(getApplication())
    }

    val isSubscribed: MutableLiveData<Boolean> = MutableLiveData()
    val processError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun toggleSubscription(podcastId: String) {
        if (isSubscribed.value == null) return
        val subscribeStatus = if (isSubscribed.value!!) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItService.subscribeOrUnsubscribe(podcastId, it, subscribeStatus)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                isSubscribed.value = subscribeStatus.toBoolean()
                                processError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable?) {
                                processError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    fun getSubscriptionStatus(podcastId: String) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItService.getSubscriptionStatus(podcastId, it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Boolean>() {
                            override fun onSuccess(result: Boolean) {
                                isSubscribed.value = result
                                processError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable?) {
                                processError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    val subscriptions: MutableLiveData<List<Subscription>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getSubscriptions(page: Int) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItService.getSubscriptions(it, page)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Subscription>>() {
                            override fun onSuccess(result: List<Subscription>) {
                                subscriptions.value = result
                                loadError.value = false
                                loading.value = false
                            }

                            override fun onError(e: Throwable?) {
                                loadError.value = true
                                loading.value = false
                            }
                        })
                )
            }
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}