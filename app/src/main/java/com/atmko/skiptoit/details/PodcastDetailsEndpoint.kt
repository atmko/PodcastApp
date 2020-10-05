package com.atmko.skiptoit.details

import com.atmko.skiptoit.model.PodcastDetails
import com.atmko.skiptoit.model.PodcastsApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class PodcastDetailsEndpoint(
    val podcastsApi: PodcastsApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onPodcastDetailsFetchSuccess(fetchedPodcastDetails: PodcastDetails)
        fun onPodcastDetailsFetchFailed()
    }

    open fun getPodcastDetails(podcastId: String, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                podcastsApi!!.getDetails(podcastId)
                    .enqueue(object : Callback<PodcastDetails> {
                        override fun onResponse(
                            call: Call<PodcastDetails>, response: Response<PodcastDetails>) {
                            if (response.isSuccessful) {
                                listener.onPodcastDetailsFetchSuccess(response.body()!!)
                            } else {
                                listener.onPodcastDetailsFetchFailed()
                            }
                        }

                        override fun onFailure(call: Call<PodcastDetails>, t: Throwable) {
                            listener.onPodcastDetailsFetchFailed()
                        }
                    })
            }
        }
    }
}