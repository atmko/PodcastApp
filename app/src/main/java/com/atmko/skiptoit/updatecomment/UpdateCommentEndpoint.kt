package com.atmko.skiptoit.updatecomment

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UpdateCommentEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onUpdateSuccess(bodyUpdate: String)
        fun onUpdateFailed()
    }

    open fun updateComment(currentComment: Comment, bodyUpdate: String, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.updateCommentBody(currentComment.commentId, it, bodyUpdate)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onUpdateSuccess(bodyUpdate)
                            } else {
                                listener.onUpdateFailed()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            listener.onUpdateFailed()
                        }
                    })
            }
        }
    }
}