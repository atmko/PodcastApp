package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

class Podcast(@SerializedName("id") val id: String,
              @SerializedName("title", alternate=["title_original"]) val title: String,
              @SerializedName("publisher", alternate=["publisher_original"]) val publisher: String,
              val image: String,
              val description: String,
              @SerializedName("total_episodes") val totalEpisodes: Int) {
}