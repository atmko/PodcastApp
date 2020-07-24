package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsService
import com.atmko.skiptoit.model.SkipToItDatabase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DetailsViewModel(private val googleSignInAccount: GoogleSignInAccount?,
                       private val skipToItDatabase: SkipToItDatabase,
                       private val podcast: Podcast): ViewModel() {

    val podcastDetails:MutableLiveData<Podcast> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: PodcastsService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
    }

    fun refresh(podcastId: String) {
        loading.value = true
        disposable.add(
            podcastService.getDetails(podcastId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Podcast>() {
                    override fun onSuccess(podcast: Podcast?) {
                        podcastDetails.value = podcast
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

    override fun onCleared() {
        disposable.clear()
    }
}