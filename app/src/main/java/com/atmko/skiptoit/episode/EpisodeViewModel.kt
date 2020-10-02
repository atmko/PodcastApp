package com.atmko.skiptoit.episode

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class EpisodeViewModel(
    private val podcastsApi: PodcastsApi,
    private val skipToItDatabase: SkipToItDatabase,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val tag = this::class.simpleName

    val episodeDetails: MutableLiveData<Episode> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val disposable: CompositeDisposable = CompositeDisposable()

    //if new podcast, erase previously played podcast's episodes from cache
    fun clearPodcastCache(currentPodcastId: String) {
        val lastPlayedPodcastId = prefs.getString(PODCAST_ID_KEY, "")
        if (lastPlayedPodcastId != null && lastPlayedPodcastId != currentPodcastId) {
            AppExecutors.getInstance().diskIO().execute {
                skipToItDatabase.episodeDao().deletePodcastEpisodes(lastPlayedPodcastId)
            }
        }
    }

    fun refresh(episodeId: String) {
        if (episodeDetails.value != null) {
            return
        }
        loading.value = true
        disposable.add(
            podcastsApi.getEpisodeDetails(episodeId)
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
        if (episodeDetails.value != null) {
            return
        }
        loading.value = true
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
                Episode(
                    episodeId!!,
                    title,
                    description,
                    image,
                    audio,
                    publishDate,
                    lengthInSeconds,
                    podcast
                )

            episode.podcastId = podcast.id

            AppExecutors.getInstance().mainThread().execute {
                episodeDetails.value = episode
                loadError.value = false
                loading.value = false
            }
        })
    }

    var nextEpisode: LiveData<Episode?>? = null
    var prevEpisode: LiveData<Episode?>? = null

    fun fetchNextEpisode(podcastId: String, episode: Episode) {
        if (nextEpisode != null && nextEpisode!!.value != null) {
            return
        }
        nextEpisode =
            skipToItDatabase.episodeDao().getNextEpisode(episode.episodeId, episode.publishDate)
        if (nextEpisode!!.value == null) {
            AppExecutors.getInstance().diskIO().execute {
                fetchNextEpisodesFromRemote(podcastId, episode.publishDate)
            }
        }
    }

    fun fetchPrevEpisode(episode: Episode) {
        if (prevEpisode != null && prevEpisode!!.value != null) {
            return
        }
        prevEpisode =
            skipToItDatabase.episodeDao().getPrevEpisode(episode.episodeId, episode.publishDate)
    }

    private fun fetchNextEpisodesFromRemote(podcastId: String, episodePublishDate: Long) {
        val episodeResultCall = podcastsApi.getEpisodes(podcastId, episodePublishDate)

        val response = episodeResultCall.execute()
        if (response.isSuccessful) {
            Log.d(tag, "isSuccessful")
            val body: PodcastDetails = response.body()!!
            onEpisodeFetchCallback(body)
        } else {
            Log.d(tag, "!isSuccessful")
        }
    }

    private fun onEpisodeFetchCallback(
        podcastDetails: PodcastDetails
    ) {
        skipToItDatabase.beginTransaction()
        try {
            val episodes = podcastDetails.episodes
            for (i in episodes) {
                i.podcastId = podcastDetails.id
            }

        } finally {
            skipToItDatabase.endTransaction()
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}