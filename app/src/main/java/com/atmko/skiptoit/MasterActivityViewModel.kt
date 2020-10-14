package com.atmko.skiptoit

import android.util.Log
import com.atmko.skiptoit.common.ManagerViewModel

class MasterActivityViewModel(
    loginManager: LoginManager,
    userEndpoint: UserEndpoint
) : ManagerViewModel(
    loginManager,
    userEndpoint
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