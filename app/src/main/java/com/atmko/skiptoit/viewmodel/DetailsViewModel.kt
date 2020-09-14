package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.atmko.skiptoit.viewmodel.paging.EpisodeBoundaryCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class DetailsViewModel(
    private val skipToItApi: SkipToItApi,
    private val podcastsApi: PodcastsApi,
    private val googleSignInClient: GoogleSignInClient,
    private val skipToItDatabase: SkipToItDatabase,
    private val episodeBoundaryCallback: EpisodeBoundaryCallback,
    private val pagedListConfig: PagedList.Config
) : ViewModel() {

    companion object {
        const val pageSize = 10
        const val enablePlaceholders = true
        const val maxSize = 60
        const val prefetchDistance = 5
        const val initialLoadSize = 30
    }

    val podcastDetails: MutableLiveData<PodcastDetails> = MutableLiveData()
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
        if (podcastDetails.value != null) {
            return
        }
        loading.value = true
        disposable.add(
            podcastsApi.getDetails(podcastId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<PodcastDetails>() {
                    override fun onSuccess(podcast: PodcastDetails) {
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

    var episodes: LiveData<PagedList<Episode>>? = null

    fun getEpisodes(podcastId: String) {
        if (episodes != null
            && episodes!!.value != null
            && !episodes!!.value!!.isEmpty()
        ) {
            return
        }

        episodeBoundaryCallback.param = podcastId
        val dataSourceFactory = skipToItDatabase.episodeDao().getAllEpisodesForPodcast(podcastId)
        val pagedListBuilder =
            LivePagedListBuilder<Int, Episode>(dataSourceFactory, pagedListConfig)
        pagedListBuilder.setInitialLoadKey(1)
        pagedListBuilder.setBoundaryCallback(episodeBoundaryCallback)
        episodes = pagedListBuilder.build()
    }

    fun loadSubscriptionStatus(podcastId: String) {
        isSubscribed = skipToItDatabase.subscriptionsDao().isSubscribed(podcastId)
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