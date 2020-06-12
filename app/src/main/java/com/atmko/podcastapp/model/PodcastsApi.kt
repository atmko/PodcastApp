package com.atmko.podcastapp.model

import com.atmko.podcastapp.BuildConfig
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PodcastsApi {
    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("best_podcasts?age=1&region=us&safe_mode=0")
    fun getPodcastsByGenre(@Query("genre_id") genreId:Int): Single<ApiResults>
}