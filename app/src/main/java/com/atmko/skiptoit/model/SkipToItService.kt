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

    override fun getComments(podcastId: String, page: Int): Single<List<Comment>> {
        return api.getComments(podcastId, page)
    }

    override fun getUser(idToken: String): Single<User> {
        return api.getUser(idToken)
    }

    override fun updateUsername(idToken: String, username: String): Single<Response<Void>> {
        return api.updateUsername(idToken, username)
    }
}