package com.atmko.skiptoit.subcriptions

import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.Subscription
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class SubscriptionsEndpoint(
    val skipToItApi: SkipToItApi?,
    val googleSignInClient: GoogleSignInClient?
) {

    interface UpdateSubscriptionListener {
        fun onSubscriptionStatusUpdated()
        fun onSubscriptionStatusUpdateFailed()
    }

    interface RetrieveSubscriptionsListener {
        fun onSubscriptionsFetchSuccess(subscriptions: List<Subscription>)
        fun onSubscriptionsFetchFailed()
    }

    open fun updateSubscription(podcastId: String, subscriptionStatus: Int, listener: UpdateSubscriptionListener) {
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

    open fun getSubscriptions(listener: RetrieveSubscriptionsListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                skipToItApi!!.getSubscriptions(it)
                    .enqueue(object : Callback<List<Subscription>> {
                        override fun onResponse(call: Call<List<Subscription>>, response: Response<List<Subscription>>) {
                            if (response.isSuccessful) {
                                listener.onSubscriptionsFetchSuccess(response.body()!!)
                            } else {
                                listener.onSubscriptionsFetchFailed()
                            }
                        }

                        override fun onFailure(call: Call<List<Subscription>>, t: Throwable) {
                            listener.onSubscriptionsFetchFailed()
                        }
                    })
            }
        }
    }
}