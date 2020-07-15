package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

const val BODY_KEY: String = "body"

class Comment(
    @SerializedName("comment_id")
    val commentId: String?,
    val username: String?,
    val body: String,
    @SerializedName("profile_image")
    val profileImage: String?) {

    constructor(body: String):
            this(null, null, body, null) {
    }
}