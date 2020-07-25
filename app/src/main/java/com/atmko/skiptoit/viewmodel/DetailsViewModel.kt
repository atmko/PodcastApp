package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsApi
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class DetailsViewModel(private val googleSignInAccount: GoogleSignInAccount?,
                       private val skipToItDatabase: SkipToItDatabase,
                       private val podcast: Podcast): ViewModel() {

    val podcastDetails:MutableLiveData<Podcast> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: PodcastsApi
    @Inject
    lateinit var skipToItService: SkipToItApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    val isSubscribed: LiveData<Boolean>
    val processError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
        isSubscribed = skipToItDatabase.subscriptionsDao().isSubscribed(podcast.id)
        processError.value = false
        processing.value = false
    }

    fun refresh(podcastId: String) {
        loading.value = true
        disposable.add(
            podcastService.getDetails(podcastId)
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

    fun toggleSubscription() {
        toggleRemoteSubscriptionStatus()
    }

    private fun toggleRemoteSubscriptionStatus() {
        val subscribeStatus = if (isSubscribed.value!!) STATUS_UNSUBSCRIBE else STATUS_SUBSCRIBE
        googleSignInAccount?.let { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItService.subscribeOrUnsubscribe(podcast.id, it, subscribeStatus)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    toggleLocalSubscriptionStatus(subscribeStatus)
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

    private fun toggleLocalSubscriptionStatus(subscribeStatus: Int) {
        AppExecutors.getInstance().diskIO().execute(Runnable {
            if (subscribeStatus == STATUS_SUBSCRIBE) {
                skipToItDatabase.subscriptionsDao().createSubscription(podcast)
            } else {
                skipToItDatabase.subscriptionsDao().deleteSubscription(podcast.id)
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