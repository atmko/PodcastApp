package com.atmko.podcastapp.model

import com.atmko.podcastapp.dependencyinjection.DaggerSkipToItApiComponent
import io.reactivex.Single
import javax.inject.Inject

class SkipToItService : SkipToItApi {
    @Inject
    lateinit var api: SkipToItApi

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    override fun getComments(podcastId: String, page: Int): Single<List<Comment>> {
        return api.getComments(podcastId, page)
    }
}