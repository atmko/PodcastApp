package com.atmko.podcastapp.model

import com.google.gson.annotations.SerializedName

class Comment(
    @SerializedName("comment_id")
    val commentId: String,
    val username: String,
    val body: String,
    @SerializedName("profile_image")
    val profileImage: String?)