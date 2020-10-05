package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.model.SkipToItApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class SubscriptionsEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface Listener {
        fun onSubscriptionStatusUpdated()
        fun onSubscriptionStatusUpdateFailed()
    }

    open fun updateSubscription(podcastId: String, subscriptionStatus: Int, listener: Listener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.subscribeOrUnsubscribe(podcastId, it, subscriptionStatus)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                listener.onSubscriptionStatusUpdated()
                            } else {
                                listener.onSubscriptionStatusUpdateFailed()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            listener.onSubscriptionStatusUpdateFailed()
                        }
                    })
            }
        }
    }
}