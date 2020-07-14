package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

const val PODCAST_ID_KEY = "podcast_id"
const val PODCAST_TITLE_KEY = "podcast_title"

class Podcast(@SerializedName("id") val id: String,
              @SerializedName("title", alternate=["title_original"]) val title: String?,
              @SerializedName("publisher", alternate=["publisher_original"]) val publisher: String,
              val image: String,
              val description: String,
              @SerializedName("total_episodes") val totalEpisodes: Int) {

    var episodes: List<Episode> = mutableListOf()

    constructor(id: String, title: String, publisher: String, image: String, description: String,
                totalEpisodes: Int, episodes: List<Episode>):
            this(id, title, publisher, image, description, totalEpisodes) {
        this.episodes = episodes
    }
}