package com.atmko.podcastapp.model

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class PodcastsService : PodcastsApi {
    private val BASE_URL = "https://listen-api.listennotes.com/api/v2/"
    private val api: PodcastsApi

    init {
        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(PodcastsApi::class.java)
    }

    override fun getPodcastsByGenre(genreId: Int): Single<ApiResults> {
        return api.getPodcastsByGenre(genreId)
    }

    override fun searchPodcasts(queryString: String): Single<ApiResults> {
        return api.searchPodcasts(queryString)
    }
}