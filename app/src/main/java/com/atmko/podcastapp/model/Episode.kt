package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

class Episode(
    val title: String,
    val description: String,
    @SerializedName("pub_date_ms")
    val publishDate: Long,
    @SerializedName("audio_length_sec")
    val lengthInSeconds: Int) {
}