package com.atmko.skiptoit.model

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

const val EPISODE_ID_KEY = "episode_id"

const val MINUTE_TO_SECONDS = 60
const val PUBLISH_DATE_FORMAT = "MMM dd, yyyy"

@Entity(tableName = "episodes")
class Episode(
    @PrimaryKey
    @ColumnInfo(name = "episode_id")
    @SerializedName("id")
    val episodeId: String,
    val title: String?,
    val description: String?,
    val image: String?,
    val audio: String?,
    @ColumnInfo(name = "publish_date")
    @SerializedName("pub_date_ms")
    val publishDate: Long,
    @ColumnInfo(name = "length_in_seconds")
    @SerializedName("audio_length_sec")
    val lengthInSeconds: Int) {

    @ColumnInfo(name = "podcast_id")
    var podcastId: String? = null
    @Ignore var podcast: Podcast? = null

    constructor(id: String, title: String?, description: String?, image: String?, audio: String?,
                publishDate: Long, lengthInSeconds: Int, podcast: Podcast):
            this(id, title, description, image, audio, publishDate, lengthInSeconds) {
        this.podcast = podcast
    }

    fun getFormattedAudioLength(): String {
        val minutes = lengthInSeconds / MINUTE_TO_SECONDS
        val seconds = lengthInSeconds - (minutes * MINUTE_TO_SECONDS)
        return "${minutes}:${seconds}"
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedPublishDate(): String {
        val dateFormat = SimpleDateFormat(PUBLISH_DATE_FORMAT)
        return dateFormat.format(Date(publishDate)).toString()
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.episodeId == newItem.episodeId
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (episodeId != other.episodeId) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (image != other.image) return false
        if (audio != other.audio) return false
        if (publishDate != other.publishDate) return false
        if (lengthInSeconds != other.lengthInSeconds) return false
        if (podcastId != other.podcastId) return false
        if (podcast != other.podcast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = episodeId.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + (audio?.hashCode() ?: 0)
        result = 31 * result + publishDate.hashCode()
        result = 31 * result + lengthInSeconds
        result = 31 * result + (podcastId?.hashCode() ?: 0)
        result = 31 * result + (podcast?.hashCode() ?: 0)
        return result
    }
}