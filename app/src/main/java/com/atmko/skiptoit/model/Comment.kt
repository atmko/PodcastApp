package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

const val BODY_KEY: String = "body"

class Comment(
    @SerializedName("comment_id")
    val commentId: String,
    @SerializedName("parent_id")
    val parentId: String,
    val username: String,
    val body: String,
    val votes: Int,
    @SerializedName("profile_image")
    val profileImage: String?)