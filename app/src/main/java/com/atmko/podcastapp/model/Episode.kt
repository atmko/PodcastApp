package com.atmko.podcastapp.model

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

const val EPISODE_ID_KEY = "episode_id"
const val MINUTE_TO_SECONDS = 60
const val PUBLISH_DATE_FORMAT = "MMM dd, yyyy"

class Episode(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val audio: String,
    @SerializedName("pub_date_ms")
    val publishDate: Long,
    @SerializedName("audio_length_sec")
    val lengthInSeconds: Int) {

    var podcast: Podcast? = null

    constructor(id: String, title: String, description: String, image: String, audio: String,
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
}