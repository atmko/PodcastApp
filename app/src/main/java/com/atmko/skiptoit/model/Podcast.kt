package com.atmko.skiptoit.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val PODCAST_ID_KEY = "podcast_id"
const val PODCAST_TITLE_KEY = "podcast_title"

@Entity(tableName = "subscriptions")
class Podcast(@PrimaryKey
              @SerializedName("id") val id: String,
              @SerializedName("title", alternate=["title_original"]) val title: String?,
              @SerializedName("publisher", alternate=["publisher_original"]) val publisher: String,
              val image: String,
              val description: String,
              @ColumnInfo(name = "total_episodes")
              @SerializedName("total_episodes") val totalEpisodes: Int) : Serializable {
    
    class PodcastDiffCallback : DiffUtil.ItemCallback<Podcast>() {
        override fun areItemsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return oldItem == newItem
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Podcast) return false
        return id == other.id
                && title == other.title
                && publisher == other.publisher
                && image == other.image
                && description == other.description
                && totalEpisodes == other.totalEpisodes
    }
}