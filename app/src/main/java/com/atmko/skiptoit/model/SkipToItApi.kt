package com.atmko.skiptoit.model

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface SkipToItApi {
    @FormUrlEncoded
    @POST("comments/{podcast_id}/{episode_id}")
    fun createComment(@Path("podcast_id") podcastId: String,
                      @Path("episode_id") episodeId: String,
                      @Field("id_token") idToken: String,
                      @Field("comment") comment: String): Single<Response<Void>>

    @GET("comments/{episode_id}/page/{page}")
    fun getComments(@Path("episode_id") episodeId: String,
                    @Path("page") page: Int): Single<List<Comment>>

    @FormUrlEncoded
    @POST("replies/{parent_id}")
    fun createReply(@Path("parent_id") parentId: String,
                    @Field("id_token") idToken: String,
                    @Field("comment") comment: String): Single<Response<Void>>

    @GET("replies/{parent_id}/page/{page}")
    fun getReplies(@Path("parent_id") parentId: String,
                   @Path("page") page: Int): Single<List<Comment>>

    @FormUrlEncoded
    @POST("users/tokensignin")
    fun getUser(@Field("id_token") idToken: String): Single<User>

    @FormUrlEncoded
    @POST("users/username")
    fun updateUsername(
        @Field("id_token") idToken: String,
        @Field("username") username: String): Single<Response<Void>>

    @FormUrlEncoded
    @POST("subscriptions/{podcast_id}")
    fun subscribeOrUnsubscribe(@Path("podcast_id") podcastId: String,
                               @Field("id_token") idToken: String,
                               @Field("subscribe") subscribe: Int): Single<Response<Void>>

    @GET("subscriptions/{podcast_id}")
    fun getSubscriptionStatus(@Path("podcast_id") podcastId: String,
                              @Query("id_token") idToken: String): Single<Boolean>

    @GET("subscriptions}")
    fun getSubscriptions(@Query("id_token") idToken: String,
                         @Query("page") page: Int): Single<List<Subscription>>
}