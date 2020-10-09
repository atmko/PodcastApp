package com.atmko.skiptoit

import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface GetUserListener {
        fun onUserFetchSuccess(user: User)
        fun onUserFetchFailed()
    }

    interface UpdateUsernameListener {
        fun onUsernameUpdateSuccess(user: User)
        fun onUsernameUpdateFailed()
    }

    open fun getMatchingUser(listener: GetUserListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.getUser(it)
                    .enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                listener.onUserFetchSuccess(response.body()!!)
                            } else {
                                listener.onUserFetchFailed()
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            listener.onUserFetchFailed()
                        }
                    })
            }
        }
    }

    fun updateUsername(username: String, listener: UpdateUsernameListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.updateUsername(it, username)
                    .enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                listener.onUsernameUpdateSuccess(response.body()!!)
                            } else {
                                listener.onUsernameUpdateFailed()
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            listener.onUsernameUpdateFailed()
                        }
                    })
            }
        }
    }
}