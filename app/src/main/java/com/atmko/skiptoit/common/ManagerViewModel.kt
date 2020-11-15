package com.atmko.skiptoit.common

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.UserEndpoint
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.model.database.EpisodesCache
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

open class ManagerViewModel(
    private val loginManager: LoginManager,
    private val userEndpoint: UserEndpoint,
    private val episodesCache: EpisodesCache
) : BaseViewModel<ManagerViewModel.Listener>() {

    interface Listener {
        fun onFirstSetup()
        fun onSubsequentSetup()
        fun notifyProcessing()
        fun onSilentSignInSuccess()
        fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int)
        fun onSignInSuccess()
        fun onSignInFailed()
        fun onUserFetchSuccess()
        fun onUserFetchFailed()
        fun onSignOutSuccess()
        fun onSignOutFailed()
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
                //todo is editing shared preferences here thread safe?
                setIsFirstSetUp(true)
                loginManager.isFirstSetUp(object : LoginManager.IsFirstSetupFetchListener {
                    override fun onIsFirstSetupFetched(isFirstSetup: Boolean) {
                        if (isFirstSetup) {
                            loginManager.clearDatabase(object : LoginManager.ClearDatabaseListener {
                                override fun onDatabaseCleared() {
                                    clearLastPlayedEpisode()
                                }

                                override fun onDatabaseClearFailed() {
                                    notifySignOutFailed()
                                }
                            })
                        } else {
                            notifySignOutFailed()
                        }
                    }
                })
            }

            override fun onSignOutFailed() {
                notifySignOutFailed()
            }
        })
    }

    private fun clearLastPlayedEpisode() {
        episodesCache.clearLastPlayedEpisode(object : EpisodesCache.ClearLastPlayedEpisodeListener {
            override fun onEpisodeClearSuccess() {
                setSubscriptionsSynced()
            }

            override fun onEpisodeClearFailed() {
                notifySignOutFailed()
            }
        })
    }

    private fun setSubscriptionsSynced() {
        loginManager.setSubscriptionsSynced(false, object : LoginManager.SyncStatusUpdateListener {
            override fun onSyncStatusUpdated() {
                notifySignOutSuccess()
            }
        })
    }

    fun onRequestResultReceived(requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                loginManager.getSignedInAccount(
                    intent,
                    object : LoginManager.GoogleAccountFetchListener {
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
            override fun onUserFetchSuccess(user: User?) {
                currentUser = user
                notifyUserFetchSuccess()
            }

            override fun onUserFetchFailed() {
                notifyUserFetchFailed()
            }
        })
    }

    fun getIsFirstSetupAndNotify() {
        loginManager.isFirstSetUp(object : LoginManager.IsFirstSetupFetchListener {
            override fun onIsFirstSetupFetched(isFirstSetup: Boolean) {
                if (isFirstSetup) {
                    notifyIsFirstSetup()
                } else {
                    notifySubsequentSetup()
                }
            }
        })
    }

    fun setIsFirstSetUp(isFirstSetUp: Boolean) {
        loginManager.setIsFirstSetup(isFirstSetUp)
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

    private fun notifyIsFirstSetup() {
        for (listener in listeners) {
            listener.onFirstSetup()
        }
    }

    private fun notifySubsequentSetup() {
        for (listener in listeners) {
            listener.onSubsequentSetup()
        }
    }

    private fun notifySilentSignInSuccess() {
        for (listener in listeners) {
            listener.onSilentSignInSuccess()
        }
    }

    private fun notifySilentSignInFailed(googleSignInIntent: Intent) {
        for (listener in listeners) {
            listener.onSilentSignInFailed(
                googleSignInIntent,
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

    private fun notifyUserFetchSuccess() {
        for (listener in listeners) {
            listener.onUserFetchSuccess()
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

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
    }
}