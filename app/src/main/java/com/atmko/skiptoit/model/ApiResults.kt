package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class ApiResults(
    @SerializedName("podcasts")
    val podcasts: List<Podcast>,
    @SerializedName("has_next")
    val hasNext: Boolean)