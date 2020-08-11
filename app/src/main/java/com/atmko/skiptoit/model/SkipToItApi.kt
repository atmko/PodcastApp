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

    @FormUrlEncoded
    @POST("comments/vote/{comment_id}")
    fun voteComment(@Path("comment_id") commentId: String,
             @Field("vote_weight") voteWeight: String,
             @Field("id_token") idToken: String): Single<Response<Void>>

    @DELETE("comments/vote/{comment_id}")
    fun deleteCommentVote(@Path("comment_id") commentId: String,
                          @Query("id_token") it: String): Single<Response<Void>>

    @GET("comments/{episode_id}/page/{page}")
    fun getCommentsUnauthenticated(@Path("episode_id") episodeId: String,
                                   @Path("page") page: Int): Single<List<Comment>>

    @GET("comments/{episode_id}/page/{page}")
    fun getCommentsAuthenticated(@Path("episode_id") episodeId: String,
                                 @Path("page") page: Int,
                                 @Query("id_token") idToken: String): Single<List<Comment>>

    @FormUrlEncoded
    @POST("replies/{parent_id}")
    fun createReply(@Path("parent_id") parentId: String,
                    @Field("id_token") idToken: String,
                    @Field("comment") comment: String): Single<Response<Void>>

    @GET("replies/{parent_id}/page/{page}")
    fun getRepliesUnauthenticated(@Path("parent_id") parentId: String,
                                  @Path("page") page: Int): Single<List<Comment>>

    @GET("replies/{parent_id}/page/{page}")
    fun getRepliesAuthenticated(@Path("parent_id") parentId: String,
                                @Path("page") page: Int,
                                @Query("id_token") idToken: String): Single<List<Comment>>

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

    @GET("subscriptions")
    fun getSubscriptions(@Query("id_token") idToken: String): Single<List<Subscription>>
}