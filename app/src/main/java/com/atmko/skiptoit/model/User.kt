package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class User(@SerializedName("user_id")
           val userId: String,
           @SerializedName("google_id")
           val googleId: String,
           @SerializedName("username")
           val username: String?,
           @SerializedName("profile_image")
           val profileImage: String)