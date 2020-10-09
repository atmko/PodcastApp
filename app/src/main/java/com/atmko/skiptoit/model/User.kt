package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class User(@SerializedName("user_id")
           val userId: String,
           @SerializedName("google_id")
           val googleId: String,
           @SerializedName("username")
           val username: String?,
           @SerializedName("profile_image")
           val profileImage: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (userId != other.userId) return false
        if (googleId != other.googleId) return false
        if (username != other.username) return false
        if (profileImage != other.profileImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + googleId.hashCode()
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + profileImage.hashCode()
        return result
    }
}