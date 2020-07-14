package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class Comment(
    @SerializedName("comment_id")
    val commentId: String?,
    val username: String,
    val body: String,
    @SerializedName("profile_image")
    val profileImage: String?) {

    constructor(username: String, body: String):
            this(null, username, body, null) {
    }
}