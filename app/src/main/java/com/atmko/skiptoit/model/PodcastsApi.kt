package com.atmko.skiptoit.model

import android.content.SharedPreferences
import com.atmko.skiptoit.BuildConfig
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface PodcastsApi {
    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("best_podcasts?age=1&region=us&safe_mode=0")
    fun getPodcastsByGenre(@Query("genre_id") genreId:Int): Single<ApiResults>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("typeahead?show_podcasts=1&show_genres=0&safe_mode=0")
    fun searchPodcasts(@Query("q") queryString:String): Single<ApiResults>

    //todo rename to getPodcastDetails
    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("podcasts/{podcast_id}?next_episode_pub_date=0000000000000&sort=recent_first")
    fun getDetails(@Path("podcast_id") podcastId:String): Single<Podcast>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("episodes/{episode_id}")
    fun getEpisodeDetails(@Path("episode_id") episodeId:String): Single<Episode>

    fun getLastPlayedEpisode(prefs: SharedPreferences): Single<Episode>
}