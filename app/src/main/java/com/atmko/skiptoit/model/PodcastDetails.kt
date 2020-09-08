package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class PodcastDetails(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("publisher") val publisher: String,
    val image: String,
    val description: String,
    @SerializedName("total_episodes") val totalEpisodes: Int,
    val episodes: List<Episode>,
    @SerializedName("next_episode_pub_date") val nextEpisodePubDate: Long?
)