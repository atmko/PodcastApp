package com.atmko.skiptoit.launch

import android.content.Intent
import android.util.Log
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.common.views.ManagerViewModel
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint

class LaunchFragmentViewModel(
    loginManager: LoginManager,
    userEndpoint: UserEndpoint,
    subscriptionsEndpoint: SubscriptionsEndpoint,
    podcastsEndpoint: PodcastsEndpoint,
    subscriptionsCache: SubscriptionsCache
) : ManagerViewModel(
    loginManager,
    userEndpoint,
    subscriptionsEndpoint,
    podcastsEndpoint,
    subscriptionsCache
) {

    interface Listener {
        fun notifyProcessing()
        fun onSilentSignInSuccess()
        fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int)
        fun onSignInSuccess()
        fun onSignInFailed()
        fun onUserFetchSuccess(user: User)
        fun onUserFetchFailed()
        fun onRestoreSubscriptionsSuccess()
        fun onRestoreSubscriptionsFailed()
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}