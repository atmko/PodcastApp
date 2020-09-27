package com.atmko.skiptoit.createcomment

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class CreateCommentEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onCreateSuccess(comment: Comment)
        fun onCreateFailed()
    }

    open fun createComment(podcastId: String, episodeId: String, commentBody: String, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.createComment(podcastId, episodeId, it, commentBody)
                    .enqueue(object : Callback<Comment> {
                        override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                            if (response.isSuccessful) {
                                listener.onCreateSuccess(response.body()!!)
                            } else {
                                listener.onCreateFailed()
                            }
                        }

                        override fun onFailure(call: Call<Comment>, t: Throwable) {
                            listener.onCreateFailed()
                        }
                    })
            }
        }
    }
}