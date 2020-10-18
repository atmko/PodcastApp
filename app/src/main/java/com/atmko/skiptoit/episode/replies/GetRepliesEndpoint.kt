package com.atmko.skiptoit.episode.replies

import com.atmko.skiptoit.model.CommentResults
import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class GetRepliesEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onQuerySuccess(commentResults: CommentResults)
        fun onQueryFailed()
    }

    open fun getReplies(param: String, loadKey: Int, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            val commentResultCall =
                skipToItApi!!.getRepliesAuthenticated(
                    param,
                    loadKey,
                    account.idToken!!
                )
            calRepliesEndpoint(commentResultCall, listener)
        }.addOnFailureListener {
            val commentResultCall =
                skipToItApi!!.getRepliesUnauthenticated(
                    param,
                    loadKey
                )
            calRepliesEndpoint(commentResultCall, listener)
        }
    }

    private fun calRepliesEndpoint(commentResultCall: Call<CommentResults>, listener: Listener) {
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