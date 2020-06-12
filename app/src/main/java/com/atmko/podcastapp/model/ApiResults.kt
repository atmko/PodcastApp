package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

class ApiResults {
    @SerializedName("podcasts")
    val podcasts: List<Podcast>? = null
}