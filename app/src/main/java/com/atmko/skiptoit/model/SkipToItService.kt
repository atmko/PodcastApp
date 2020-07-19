package com.atmko.skiptoit.model

import com.atmko.skiptoit.dependencyinjection.DaggerSkipToItApiComponent
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

class SkipToItService : SkipToItApi {
    @Inject
    lateinit var api: SkipToItApi

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    override fun createComment(podcastId: String, episodeId: String,
                               idToken: String, comment: String): Single<Response<Void>> {
        return api.createComment(podcastId, episodeId, idToken, comment)
    }

    override fun getComments(episodeId: String, page: Int): Single<List<Comment>> {
        return api.getComments(episodeId, page)
    }

    override fun createReply(parentId: String,
                             idToken: String,
                             comment: String): Single<Response<Void>> {
        return api.createReply(parentId, idToken, comment)
    }

    override fun getReplies(parentId: String, page: Int): Single<List<Comment>> {
        return api.getReplies(parentId, page)
    }

    override fun getUser(idToken: String): Single<User> {
        return api.getUser(idToken)
    }

    override fun updateUsername(idToken: String, username: String): Single<Response<Void>> {
        return api.updateUsername(idToken, username)
    }

    override fun subscribeOrUnsubscribe(podcastId: String,
                                        idToken: String,
                                        subscribe: Int): Single<Response<Void>> {
        return api.subscribeOrUnsubscribe(podcastId, idToken, subscribe)
    }

    override fun getSubscriptionStatus(podcastId: String, idToken: String): Single<Boolean> {
        return api.getSubscriptionStatus(podcastId, idToken)
    }

    override fun getSubscriptions(idToken: String, page: Int): Single<List<Subscription>> {
        return api.getSubscriptions(idToken, page)
    }
}