package com.atmko.skiptoit.episodelist

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.PodcastsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class GetEpisodesEndpoint(
    val podcastsApi: PodcastsApi?) {

    interface Listener {
        fun onEpisodesQuerySuccess(podcastDetails: PodcastDetails)
        fun onEpisodesQueryFailed()
    }

    open fun getEpisodes(podcastId: String, publishedAfterDate: Long?, listener: Listener) {
        podcastsApi!!.getEpisodes(podcastId, publishedAfterDate)
            .enqueue(object : Callback<PodcastDetails> {
                override fun onResponse(call: Call<PodcastDetails>, response: Response<PodcastDetails>) {
                    if (response.isSuccessful) {
                        listener.onEpisodesQuerySuccess(response.body()!!)
                    } else {
                        listener.onEpisodesQueryFailed()
                    }
                }

                override fun onFailure(call: Call<PodcastDetails>, t: Throwable) {
                    listener.onEpisodesQueryFailed()
                }
            })
    }
}