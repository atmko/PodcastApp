package com.atmko.skiptoit.model

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface SkipToItApi {
    @FormUrlEncoded
    @POST("{podcast_id}/{episode_id}/comments")
    fun createComment(@Path("podcast_id") podcastId: String,
                      @Field("parent_id") parentId: String?,
                      @Path("episode_id") episodeId: String,
                      @Field("id_token") idToken: String,
                      @Field("comment") comment: String): Single<Response<Void>>

    @GET("{podcast_id}/{episode_id}/comments/page/{page}")
    fun getComments(@Path("podcast_id") podcastId: String,
                    @Path("episode_id") episodeId: String,
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