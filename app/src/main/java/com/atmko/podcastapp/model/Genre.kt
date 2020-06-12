package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

const val GENRE_ID_KEY = "genre"

class Genre(@SerializedName("id") val id: Int)