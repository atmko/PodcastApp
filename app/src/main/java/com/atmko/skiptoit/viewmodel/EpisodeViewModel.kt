package com.atmko.skiptoit.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.application.DaggerApplicationComponent
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.util.AppExecutors
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
        DaggerApplicationComponent.create().inject(this)
    }

    fun refresh(episodeId: String) {
        loading.value = true
        disposable.add(
            podcastService.getEpisodeDetails(episodeId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Episode>() {
                    override fun onSuccess(episode: Episode) {
                        episodeDetails.value = episode
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

    fun restoreEpisode() {
        loading.value = true

        val application = getApplication<SkipToItApplication>()
        val prefs = application
            .getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)

        AppExecutors.getInstance().diskIO().execute(Runnable {
            val podcastId = prefs.getString(PODCAST_ID_KEY, "")!!
            val episodeId = prefs.getString(EPISODE_ID_KEY, "")
            val title = prefs.getString(EPISODE_TITLE_KEY, "")
            val description = prefs.getString(EPISODE_DESCRIPTION_KEY, "")
            val image = prefs.getString(EPISODE_IMAGE_KEY, "")
            val audio = prefs.getString(EPISODE_AUDIO_KEY, "")
            val publishDate = prefs.getLong(EPISODE_PUBLISH_DATE_KEY, 0)
            val lengthInSeconds = prefs.getInt(EPISODE_LENGTH_IN_SECONDS_KEY, 0)

            val podcastTitle = prefs.getString(PODCAST_TITLE_KEY, "")

            val podcast =
                Podcast(podcastId, podcastTitle, "", "", "", 0)

            val episode =
                Episode(episodeId, title,description, image, audio, publishDate, lengthInSeconds, podcast)

            AppExecutors.getInstance().mainThread().execute(Runnable {
                episodeDetails.value = episode
                loadError.value = false
                loading.value = false
            })
        })
    }

    override fun onCleared() {
        disposable.clear()
    }
}