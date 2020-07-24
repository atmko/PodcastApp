package com.atmko.skiptoit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.SkipToItDatabase
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
    lateinit var skipToItService: SkipToItApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
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

    var subscriptions: LiveData<List<Podcast>>? = null
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getSubscriptions() {
        subscriptions = SkipToItDatabase.getInstance(getApplication())
            .subscriptionsDao().getAllSubscriptions()
    }

    override fun onCleared() {
        disposable.clear()
    }
}