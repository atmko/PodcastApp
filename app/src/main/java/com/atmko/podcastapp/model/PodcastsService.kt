package com.atmko.podcastapp.model

import com.atmko.podcastapp.di.DaggerApiComponent
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
}