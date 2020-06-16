package com.atmko.podcastapp.model

import com.atmko.podcastapp.dependencyinjection.DaggerApiComponent
import io.reactivex.Single
import javax.inject.Inject

class PodcastsService : PodcastsApi {
    @Inject
    lateinit var api: PodcastsApi

    init {
        DaggerApiComponent.create().inject(this)
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
}