package com.atmko.skiptoit.confirmation

import android.util.Log
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.common.BaseViewModel
import com.atmko.skiptoit.model.User

class ConfirmationViewModel(
    private val userEndpoint: UserEndpoint
) : BaseViewModel<ConfirmationViewModel.Listener>() {

    interface Listener {
        fun notifyProcessing()
        fun onUsernameUpdated(user: User)
        fun onUsernameUpdateFailed()
    }

    fun updateUsernameAndNotify(username: String) {
        notifyProcessing()
        userEndpoint.updateUsername(username, object :
            UserEndpoint.UpdateUsernameListener {
            override fun onUsernameUpdateSuccess(user: User) {
                notifyUserFetchSuccess(user)
            }

            override fun onUsernameUpdateFailed() {
                notifyUserFetchFailed()
            }
        })
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

    private fun notifyUserFetchSuccess(user: User) {
        for (listener in listeners) {
            listener.onUsernameUpdated(user)
        }
    }

    private fun notifyUserFetchFailed() {
        for (listener in listeners) {
            listener.onUsernameUpdateFailed()
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}