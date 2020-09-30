package com.atmko.skiptoit.episode.common

import com.atmko.skiptoit.model.Comment
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
        fun onVoteSuccess(comment: Comment)
        fun onVoteFailed()
    }

    interface DeleteListener {
        fun onDeleteSuccess()
        fun onDeleteFailed()
    }

    open fun voteComment(comment: Comment, voteWeight: Int, listener: VoteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.voteComment(comment.commentId, voteWeight.toString(), it)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onVoteSuccess(comment)
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

    open fun deleteCommentVote(comment: Comment, listener: VoteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.deleteCommentVote(comment.commentId, it)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onVoteSuccess(comment)
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

    open fun deleteComment(comment: Comment, listener: DeleteListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.deleteComment(comment.commentId, it)
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