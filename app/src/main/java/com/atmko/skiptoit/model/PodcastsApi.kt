package com.atmko.skiptoit.model

import com.atmko.skiptoit.BuildConfig
import retrofit2.Call
import retrofit2.http.*

interface PodcastsApi {
    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("best_podcasts?region=us&safe_mode=0")
    fun getPodcastsByGenre(
        @Query("genre_id") genreId: Int,
        @Query("page") page: Int
    ): Call<ApiResults>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("typeahead?show_podcasts=1&show_genres=0&safe_mode=0")
    fun searchPodcasts(@Query("q") queryString: String, @Query("page") page: Int): Call<ApiResults>

    //todo rename to getPodcastDetails
    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("podcasts/{podcast_id}?next_episode_pub_date=0000000000000&sort=recent_first")
    fun getDetails(@Path("podcast_id") podcastId: String): Call<PodcastDetails>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("podcasts/{podcast_id}?sort=recent_first")
    fun getEpisodes(
        @Path("podcast_id") podcastId: String,
        @Query("next_episode_pub_date") nextEpisodePubDate: Long?
    ): Call<PodcastDetails>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @GET("episodes/{episode_id}")
    fun getEpisodeDetails(@Path("episode_id") episodeId: String): Call<Episode>

    @Headers("X-ListenAPI-Key: ${BuildConfig.apiKey}")
    @FormUrlEncoded
    @POST("podcasts")
    fun getBatchPodcastMetadata(@Field("ids") combinedPodcastIds: String): Call<ApiResults>
}