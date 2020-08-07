package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsApi
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class DetailsViewModel(private val skipToItApi: SkipToItApi,
                       private val podcastsApi: PodcastsApi,
                       private val googleSignInClient: GoogleSignInClient,
                       private val subscriptionsDao: SubscriptionsDao): ViewModel() {

    val podcastDetails:MutableLiveData<Podcast> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val disposable: CompositeDisposable = CompositeDisposable()

    var isSubscribed: LiveData<Boolean>? = null
    val processError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    init {
        processError.value = false
        processing.value = false
    }

    fun refresh(podcastId: String) {
        loading.value = true
        disposable.add(
            podcastsApi.getDetails(podcastId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Podcast>() {
                    override fun onSuccess(podcast: Podcast) {
                        podcastDetails.value = podcast
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

    public fun loadSubscriptionstatus(podcastId: String) {
        isSubscribed = subscriptionsDao.isSubscribed(podcastId)
    }

    fun toggleSubscription(podcast: Podcast) {
        toggleRemoteSubscriptionStatus(podcast)
    }

    private fun toggleRemoteSubscriptionStatus(podcast: Podcast) {
        val subscribeStatus = if (isSubscribed!!.value!!) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItApi.subscribeOrUnsubscribe(podcast.id, it, subscribeStatus)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    toggleLocalSubscriptionStatus(podcast, subscribeStatus)
                                } else {
                                    processError.value = true
                                    processing.value = false
                                }
                            }

                            override fun onError(e: Throwable) {
                                processError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    private fun toggleLocalSubscriptionStatus(podcast: Podcast, subscribeStatus: Int) {
        AppExecutors.getInstance().diskIO().execute(Runnable {
            if (subscribeStatus == STATUS_SUBSCRIBE) {
                subscriptionsDao.createSubscription(podcast)
            } else {
                subscriptionsDao.deleteSubscription(podcast.id)
            }

            AppExecutors.getInstance().mainThread().execute(Runnable {
                processError.value = false
                processing.value = false
            })
        })
    }

    override fun onCleared() {
        disposable.clear()
    }
}