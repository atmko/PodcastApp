package com.atmko.skiptoit.episode.common

import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class CommentsEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface VoteListener {
        fun onVoteSuccess()
        fun onVoteFailed()
    }

    interface DeleteListener {
        fun onDeleteSuccess()
        fun onDeleteFailed()
    }

    open fun voteComment(commentId: String, voteWeight: Int, listener: VoteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.voteComment(commentId, voteWeight.toString(), it)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onVoteSuccess()
                            } else {
                                listener.onVoteFailed()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            listener.onVoteFailed()
                        }
                    })
            }
        }
    }

    open fun deleteCommentVote(commentId: String, listener: VoteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.deleteCommentVote(commentId, it)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onVoteSuccess()
                            } else {
                                listener.onVoteFailed()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            listener.onVoteFailed()
                        }
                    })
            }
        }
    }

    open fun deleteComment(commentId: String, listener: DeleteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.deleteComment(commentId, it)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onDeleteSuccess()
                            } else {
                                listener.onDeleteFailed()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            listener.onDeleteFailed()
                        }
                    })
            }
        }
    }
}