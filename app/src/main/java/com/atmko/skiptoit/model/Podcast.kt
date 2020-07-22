package com.atmko.skiptoit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

const val PODCAST_ID_KEY = "podcast_id"
const val PODCAST_TITLE_KEY = "podcast_title"

@Entity(tableName = "subscriptions")
class Podcast(@SerializedName("id") val id: String,
              @SerializedName("title", alternate=["title_original"]) val title: String?,
              @SerializedName("publisher", alternate=["publisher_original"]) val publisher: String,
              val image: String,
              val description: String,
              @ColumnInfo(name = "total_episodes")
              @SerializedName("total_episodes") val totalEpisodes: Int) {

    @Ignore
    var episodes: List<Episode> = mutableListOf()

    @Ignore
    constructor(id: String, title: String, publisher: String, image: String, description: String,
                totalEpisodes: Int, episodes: List<Episode>):
            this(id, title, publisher, image, description, totalEpisodes) {
        this.episodes = episodes
    }
}