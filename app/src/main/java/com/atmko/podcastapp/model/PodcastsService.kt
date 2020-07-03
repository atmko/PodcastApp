package com.atmko.podcastapp.model

import android.content.SharedPreferences
import com.atmko.podcastapp.dependencyinjection.DaggerListenNotesApiComponent
import io.reactivex.Single
import javax.inject.Inject

class PodcastsService : PodcastsApi {
    @Inject
    lateinit var api: PodcastsApi

    init {
        DaggerListenNotesApiComponent.create().inject(this)
    }

    override fun getPodcastsByGenre(genreId: Int): Single<ApiResults> {
        return api.getPodcastsByGenre(genreId)
    }

    override fun searchPodcasts(queryString: String): Single<ApiResults> {
        return api.searchPodcasts(queryString)
    }

    override fun getDetails(podcastId: String): Single<Podcast> {
        return api.getDetails(podcastId)
    }

    override fun getEpisodeDetails(episodeId: String): Single<Episode> {
        return api.getEpisodeDetails(episodeId)
    }

    override fun getLastPlayedEpisode(prefs: SharedPreferences): Single<Episode> {
        val id = prefs.getString(EPISODE_ID_KEY, "")
        val title = prefs.getString(EPISODE_TITLE_KEY, "")
        val description = prefs.getString(EPISODE_DESCRIPTION_KEY, "")
        val image = prefs.getString(EPISODE_IMAGE_KEY, "")
        val audio = prefs.getString(EPISODE_AUDIO_KEY, "")
        val publishDate = prefs.getLong(EPISODE_PUBLISH_DATE_KEY, 0)
        val lengthInSeconds = prefs.getInt(EPISODE_LENGTH_IN_SECONDS_KEY, 0)

        val podcastTitle = prefs.getString(PODCAST_TITLE_KEY, "")

        val podcast =
            Podcast("", podcastTitle, "", "", "", 0)

        val episode =
            Episode(id, title,description, image, audio, publishDate, lengthInSeconds, podcast)

        return Single.just(episode)
    }
}