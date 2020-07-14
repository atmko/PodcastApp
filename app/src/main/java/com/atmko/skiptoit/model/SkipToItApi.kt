package com.atmko.skiptoit.model

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface SkipToItApi {
    @GET("{podcast_id}/comments/page/{page}")
    fun getComments(@Path("podcast_id") podcastId: String,
                    @Path("page") page: Int): Single<List<Comment>>

    @FormUrlEncoded
    @POST("users/tokensignin")
    fun getUser(@Field("id_token") idToken: String): Single<User>

    @FormUrlEncoded
    @POST("users/username")
    fun updateUsername(
        @Field("id_token") idToken: String,
        @Field("username") username: String): Single<Response<Void>>
}