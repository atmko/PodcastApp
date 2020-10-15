package com.atmko.skiptoit

import android.util.Log
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.model.database.EpisodesCache

class MasterActivityViewModel(
    loginManager: LoginManager,
    userEndpoint: UserEndpoint,
    episodesCache: EpisodesCache
) : ManagerViewModel(
    loginManager,
    userEndpoint,
    episodesCache
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