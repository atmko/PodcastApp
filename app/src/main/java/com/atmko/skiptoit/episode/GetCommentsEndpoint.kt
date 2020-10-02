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
                if (account != null && account.idToken != null) {
                    skipToItApi!!.getCommentsAuthenticated(
                        param,
                        loadKey,
                        account.idToken!!
                    )
                } else {
                    skipToItApi!!.getCommentsUnauthenticated(
                        param,
                        loadKey
                    )
                }

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
                }
            )
        }
    }
}