package com.atmko.podcastapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.podcastapp.dependencyinjection.DaggerApiComponent
import com.atmko.podcastapp.model.Episode
import com.atmko.podcastapp.model.PodcastsService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EpisodeViewModel: ViewModel() {
    val episodeDetails:MutableLiveData<Episode> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: PodcastsService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerApiComponent.create().inject(this)
    }

    fun refresh(episodeId: String) {
        loading.value = true
        disposable.add(
            podcastService.getEpisodeDetails(episodeId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Episode>() {
                    override fun onSuccess(episode: Episode?) {
                        episodeDetails.value = episode
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