package com.atmko.skiptoit.common.views

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.Subscription
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

open class ManagerViewModel(
    private val loginManager: LoginManager,
    private val userEndpoint: UserEndpoint,
    private val subscriptionsEndpoint: SubscriptionsEndpoint,
    private val podcastsEndpoint: PodcastsEndpoint,
    private val subscriptionsCache: SubscriptionsCache
) : BaseViewModel<ManagerViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onSilentSignInSuccess()
        fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int)
        fun onSignInSuccess()
        fun onSignInFailed()
        fun onUserFetchSuccess(user: User)
        fun onUserFetchFailed()
        fun onSignOutSuccess()
        fun onSignOutFailed()
        fun onRestoreSubscriptionsSuccess()
        fun onRestoreSubscriptionsFailed()
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN: Int = 547
    }

    var currentUser: User? = null

    fun silentSignInAndNotify() {
        if (currentUser != null) return

        notifyProcessing()

        loginManager.silentSignIn(object : LoginManager.SignInListener {
            override fun onSignInSuccess(googleSignInAccount: GoogleSignInAccount) {
                notifySilentSignInSuccess()
            }

            override fun onSignInFailed(googleSignInIntent: Intent) {
                notifySilentSignInFailed(googleSignInIntent)
            }
        })
    }

    fun signOutAndNotify() {
        notifyProcessing()
        loginManager.signOut(object : LoginManager.SignOutListener {
            override fun onSignOutSuccess() {
                currentUser = null
                notifySignOutSuccess()
            }

            override fun onSignOutFailed() {
                notifySignOutFailed()
            }
        })
    }

    fun onRequestResultReceived(requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                loginManager.getSignedInAccount(intent, object : LoginManager.GoogleAccountFetchListener {
                    override fun onAccountFetchSuccess() {
                        notifySignInSuccess()
                    }

                    override fun onAccountFetchFailed() {
                        notifySignInFailed()
                    }
                })
            } else {
                notifySignInFailed()
            }
        }
    }

    fun getMatchingUserAndNotify() {
        if (currentUser != null) {
            return
        }

        notifyProcessing()

        userEndpoint.getMatchingUser(object : UserEndpoint.GetUserListener {
            override fun onUserFetchSuccess(user: User) {
                currentUser = user
                notifyUserFetchSuccess(currentUser!!)
            }

            override fun onUserFetchFailed() {
                notifyUserFetchFailed()
            }
        })
    }

    fun restoreSubscriptionsAndNotify() {
        notifyProcessing()
        subscriptionsEndpoint.getSubscriptions(object : SubscriptionsEndpoint.RetrieveSubscriptionsListener {
            override fun onSubscriptionsFetchSuccess(subscriptions: List<Subscription>) {
                getBatchPodcastData(subscriptions)
            }

            override fun onSubscriptionsFetchFailed() {
                notifyRestoreSubscriptionsFailed()
            }
        })
    }

    fun isFirstSetUp(): Boolean {
        return loginManager.isFirstSetUp()
    }

    fun setIsFirstSetUp(isFirstSetUp: Boolean) {
        loginManager.setIsFirstSetup(isFirstSetUp)
    }

    private fun getBatchPodcastData(subscriptions: List<Subscription>) {
        podcastsEndpoint.getBatchPodcastMetadata(combinePodcastIds(subscriptions), object : PodcastsEndpoint.BatchFetchPodcastsListener {
            override fun onBatchFetchSuccess(apiResults: ApiResults) {
                saveToLocalDatabase(apiResults.podcasts)
            }

            override fun onBatchFetchFailed() {
                notifyRestoreSubscriptionsFailed()
            }
        })
    }

    private fun saveToLocalDatabase(podcasts: List<Podcast>) {
        subscriptionsCache.insertSubscription(podcasts, object : SubscriptionsCache.SubscriptionUpdateListener {
            override fun onSubscriptionUpdateSuccess() {
                notifyRestoreSubscriptionsSuccess()
            }

            override fun onSubscriptionUpdateFailed() {
                notifyRestoreSubscriptionsFailed()
            }
        })
    }

    private fun combinePodcastIds(subscriptions: List<Subscription>): String {
        val builder: StringBuilder = java.lang.StringBuilder()
        var counter = 0
        while (counter < subscriptions.size) {
            builder.append(subscriptions[counter].listenNotesId)
            if (counter != subscriptions.size - 1) {
                builder.append(",")
            }
            counter += 1
        }

        return builder.toString()
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    private fun notifyProcessing() {
        for (listener in listeners) {
            listener.notifyProcessing()
        }
    }

    private fun notifySilentSignInSuccess() {
        for (listener in listeners) {
            listener.onSilentSignInSuccess()
        }
    }

    private fun notifySilentSignInFailed(googleSignInIntent: Intent) {
        for (listener in listeners) {
            listener.onSilentSignInFailed(googleSignInIntent,
                REQUEST_CODE_SIGN_IN
            )
        }
    }

    private fun notifySignInSuccess() {
        for (listener in listeners) {
            listener.onSignInSuccess()
        }
    }

    private fun notifySignInFailed() {
        for (listener in listeners) {
            listener.onSignInFailed()
        }
    }

    private fun notifyUserFetchSuccess(user: User) {
        for (listener in listeners) {
            listener.onUserFetchSuccess(user)
        }
    }

    private fun notifyUserFetchFailed() {
        for (listener in listeners) {
            listener.onUserFetchFailed()
        }
    }

    private fun notifySignOutSuccess() {
        for (listener in listeners) {
            listener.onSignOutSuccess()
        }
    }

    private fun notifySignOutFailed() {
        for (listener in listeners) {
            listener.onSignOutFailed()
        }
    }

    private fun notifyRestoreSubscriptionsSuccess() {
        for (listener in listeners) {
            listener.onRestoreSubscriptionsSuccess()
        }
    }

    private fun notifyRestoreSubscriptionsFailed() {
        for (listener in listeners) {
            listener.onRestoreSubscriptionsFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}