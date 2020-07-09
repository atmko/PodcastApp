package com.atmko.podcastapp.model

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface SkipToItApi {
    @GET("{podcast_id}/comments/page/{page}")
    fun getComments(@Path("podcast_id") podcastId: String,
                    @Path("page") page: Int): Single<List<Comment>>
}