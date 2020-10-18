package com.atmko.skiptoit.episode

import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class GetCommentsEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onQuerySuccess(commentResults: CommentResults)
        fun onQueryFailed()
    }

    open fun getComments(param: String, loadKey: Int, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            val commentResultCall =
                skipToItApi!!.getCommentsAuthenticated(
                    param,
                    loadKey,
                    account.idToken!!
                )
            callCommentsEndpoint(commentResultCall, listener)
        }.addOnFailureListener {
            val commentResultCall =
                skipToItApi!!.getCommentsUnauthenticated(
                    param,
                    loadKey
                )
            callCommentsEndpoint(commentResultCall, listener)
        }
    }

    private fun callCommentsEndpoint(commentResultCall: Call<CommentResults>, listener: Listener) {
        commentResultCall.enqueue(object : Callback<CommentResults> {
            override fun onResponse(call: Call<CommentResults>, response: Response<CommentResults>) {
                if (response.isSuccessful) {
                    listener.onQuerySuccess(response.body()!!)
                } else {
                    listener.onQueryFailed()
                }
            }

            override fun onFailure(call: Call<CommentResults>, t: Throwable) {
                listener.onQueryFailed()
            }
        })
    }
}