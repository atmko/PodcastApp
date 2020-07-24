package com.atmko.skiptoit.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.view.EPISODE_FRAGMENT_KEY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EpisodeViewModel(application: Application): AndroidViewModel(application) {
    val episodeDetails:MutableLiveData<Episode> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var podcastService: PodcastsApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
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

    fun restoreEpisode() {
        loading.value = true

        val application = getApplication<MultiDexApplication>()
        val prefs = application
                .getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)

        disposable.add(
            podcastService.getLastPlayedEpisode(prefs)
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