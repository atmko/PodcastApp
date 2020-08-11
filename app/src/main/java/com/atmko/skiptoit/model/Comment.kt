package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

const val BODY_KEY: String = "body"

const val VOTE_WEIGHT_NONE: Int = 0
const val VOTE_WEIGHT_UP_VOTE: Int = 1
const val VOTE_WEIGHT_DOWN_VOTE: Int = -1
const val VOTE_WEIGHT_UP_VOTE_INVERT: Int = -2
const val VOTE_WEIGHT_DOWN_VOTE_INVERT: Int = 2

class Comment(
    @SerializedName("comment_id")
    val commentId: String,
    @SerializedName("parent_id")
    val parentId: String,
    val username: String,
    val body: String,
    @SerializedName("vote_tally")
    var voteTally: Int,
    @SerializedName("vote_weight")
    var voteWeight: Int,
    val replies: Int,
    @SerializedName("profile_image")
    val profileImage: String?)