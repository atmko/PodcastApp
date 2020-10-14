package com.atmko.skiptoit.launch

import android.util.Log
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.common.ManagerViewModel

class LaunchFragmentViewModel(
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