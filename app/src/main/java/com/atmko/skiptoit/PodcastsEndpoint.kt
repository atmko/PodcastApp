package com.atmko.skiptoit

import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.PodcastsApi
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class PodcastsEndpoint(
    val podcastsApi: PodcastsApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface BatchFetchPodcastsListener {
        fun onBatchFetchSuccess(apiResults: ApiResults)
        fun onBatchFetchFailed()
    }

    open fun getBatchPodcastMetadata(combinedPodcastIds: String, listener: BatchFetchPodcastsListener) {
        podcastsApi!!.getBatchPodcastMetadata(combinedPodcastIds)
            .enqueue(object : Callback<ApiResults> {
                override fun onResponse(call: Call<ApiResults>, response: Response<ApiResults>) {
                    if (response.isSuccessful) {
                        listener.onBatchFetchSuccess(response.body()!!)
                    } else {
                        listener.onBatchFetchFailed()
                    }
                }

                override fun onFailure(call: Call<ApiResults>, t: Throwable) {
                    listener.onBatchFetchFailed()
                }
            })
    }
}