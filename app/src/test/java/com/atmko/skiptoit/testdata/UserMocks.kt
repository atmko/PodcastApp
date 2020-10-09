package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.User

class UserMocks {

    companion object {
        const val USER_ID = "userId"
        const val GOOGLE_ID = "googleId"
        const val USERNAME = "username"
        const val PROFILE_IMAGE = "profileImage"

        fun GET_USER(): User {
            return User(USER_ID, GOOGLE_ID, USERNAME, PROFILE_IMAGE)
        }
    }
}