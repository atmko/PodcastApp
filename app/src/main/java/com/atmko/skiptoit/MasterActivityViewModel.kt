package com.atmko.skiptoit

import android.util.Log
import com.atmko.skiptoit.common.views.ManagerViewModel
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint

class MasterActivityViewModel(
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