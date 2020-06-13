package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

const val GENRE_ID_KEY = "genre_id"
const val GENRE_NAME_KEY = "genre_name"

class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String)