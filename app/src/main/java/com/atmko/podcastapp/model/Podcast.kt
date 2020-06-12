package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

class Podcast(@SerializedName("id") val id: String,
              @SerializedName("title") val title: String,
              @SerializedName("publisher") val publisher: String,
              val description: String,
              @SerializedName("total_episodes") val totalEpisodes: String) {
}