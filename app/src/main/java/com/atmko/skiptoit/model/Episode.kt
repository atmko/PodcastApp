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
const val EPISODE_TITLE_KEY = "episode_title"
const val EPISODE_DESCRIPTION_KEY = "episode_description"
const val EPISODE_IMAGE_KEY = "episode_image"
const val EPISODE_AUDIO_KEY = "episode_audio"
const val EPISODE_PUBLISH_DATE_KEY = "episode_publish_date"
const val EPISODE_LENGTH_IN_SECONDS_KEY = "episode_length_in_seconds"

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
        if (other !is Episode) return false
        return episodeId == other.episodeId
                && title == other.title
                && description == other.description
                && image == other.image
                && audio == other.audio
                && publishDate == other.publishDate
                && lengthInSeconds == other.lengthInSeconds
                && podcast == other.podcast
    }
}