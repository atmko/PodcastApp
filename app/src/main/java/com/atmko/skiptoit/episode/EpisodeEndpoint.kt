package com.atmko.skiptoit.episode

import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.PodcastsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class EpisodeEndpoint(
    val podcastsApi: PodcastsApi?
) {

    interface EpisodeDetailsListener {
        fun onEpisodeDetailsFetchSuccess(episode: Episode)
        fun onEpisodeDetailsFetchFailed()
    }

    interface BatchNextEpisodeListener {
        fun onBatchNextEpisodesFetchSuccess(podcastDetails: PodcastDetails)
        fun onBatchNextEpisodesFetchFailed()
    }

    open fun getEpisodeDetails(episodeId: String, listener: EpisodeDetailsListener) {
        podcastsApi!!.getEpisodeDetails(episodeId)
            .enqueue(object : Callback<Episode> {
                override fun onResponse(call: Call<Episode>, response: Response<Episode>) {
                    if (response.isSuccessful) {
                        listener.onEpisodeDetailsFetchSuccess(response.body()!!)
                    } else {
                        listener.onEpisodeDetailsFetchFailed()
                    }
                }

                override fun onFailure(call: Call<Episode>, t: Throwable) {
                    listener.onEpisodeDetailsFetchFailed()
                }
            })
    }

    open fun fetchNextEpisodes(podcastId: String, episodePublishDate: Long, listener: BatchNextEpisodeListener) {
        podcastsApi!!.getEpisodes(podcastId, episodePublishDate)
            .enqueue(object : Callback<PodcastDetails> {
                override fun onResponse(call: Call<PodcastDetails>, response: Response<PodcastDetails>) {
                    if (response.isSuccessful) {
                        listener.onBatchNextEpisodesFetchSuccess(response.body()!!)
                    } else {
                        listener.onBatchNextEpisodesFetchFailed()
                    }
                }

                override fun onFailure(call: Call<PodcastDetails>, t: Throwable) {
                    listener.onBatchNextEpisodesFetchFailed()
                }
            })
    }
}