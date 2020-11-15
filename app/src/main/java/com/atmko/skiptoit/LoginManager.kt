package com.atmko.skiptoit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import com.atmko.skiptoit.launch.IS_FIRST_SETUP_KEY
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.atmko.skiptoit.utils.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

open class LoginManager(
    private val googleSignInClient: GoogleSignInClient?,
    private val sharedPreferences: SharedPreferences?,
    private val skipToItDatabase: SkipToItDatabase?,
    private val appExecutors: AppExecutors?
) {

    companion object {
        const val LOGIN_MANAGER_KEY = "login_manager"
    }

    interface SignInListener {
        fun onSignInSuccess(googleSignInAccount: GoogleSignInAccount)
        fun onSignInFailed(googleSignInIntent: Intent)
    }

    interface SignOutListener {
        fun onSignOutSuccess()
        fun onSignOutFailed()
    }

    interface GoogleAccountFetchListener {
        fun onAccountFetchSuccess()
        fun onAccountFetchFailed()
    }

    interface IsFirstSetupFetchListener {
        fun onIsFirstSetupFetched(isFirstSetup: Boolean)
    }

    interface SyncStatusUpdateListener {
        fun onSyncStatusUpdated()
    }

    interface ClearDatabaseListener {
        fun onDatabaseCleared()
        fun onDatabaseClearFailed()
    }

    open fun silentSignIn(listener: SignInListener) {
        googleSignInClient!!.silentSignIn().addOnSuccessListener { account ->
            listener.onSignInSuccess(account)
        }.addOnFailureListener {
            listener.onSignInFailed(googleSignInClient.signInIntent)
        }
    }

    open fun getSignedInAccount(intent: Intent, listener: GoogleAccountFetchListener) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            val account: GoogleSignInAccount? = task.result
            account?.let {
                listener.onAccountFetchSuccess()
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            listener.onAccountFetchFailed()
        }
    }

    //----------------------------------------------------------------------------------------------

    @SuppressLint("ApplySharedPref")
    open fun setSubscriptionsSynced(
        isSubscriptionsSynced: Boolean,
        listener: SyncStatusUpdateListener
    ) {
        appExecutors!!.diskIO.execute {
            sharedPreferences!!.edit()
                .putBoolean(SubscriptionsCache.IS_SUBSCRIPTIONS_SYNCED_KEY, isSubscriptionsSynced)
                .commit()

            appExecutors.mainThread.execute {
                listener.onSyncStatusUpdated()
            }
        }
    }

    open fun isSubscriptionsSynced(listener: SubscriptionsCache.SyncStatusFetchListener) {
        appExecutors!!.diskIO.execute {
            val isSubscriptionsSynced = sharedPreferences!!.getBoolean(
                SubscriptionsCache.IS_SUBSCRIPTIONS_SYNCED_KEY,
                false
            )

            appExecutors.mainThread.execute {
                listener.onSyncStatusFetched(isSubscriptionsSynced)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    open fun signOut(listener: SignOutListener) {
        googleSignInClient!!.signOut().addOnSuccessListener {
            listener.onSignOutSuccess()
        }.addOnFailureListener {
            listener.onSignOutFailed()
        }
    }

    //----------------------------------------------------------------------------------------------

    open fun isFirstSetUp(listener: IsFirstSetupFetchListener) {
        appExecutors!!.diskIO.execute {
            val isFirstSetup = sharedPreferences!!.getBoolean(IS_FIRST_SETUP_KEY, true)

            appExecutors.mainThread.execute {
                listener.onIsFirstSetupFetched(isFirstSetup)
            }
        }
    }

    open fun setIsFirstSetup(isFirstSetup: Boolean) {
        sharedPreferences!!.edit().putBoolean(IS_FIRST_SETUP_KEY, isFirstSetup).apply()
    }

    //----------------------------------------------------------------------------------------------

    open fun clearDatabase(listener: ClearDatabaseListener) {
        appExecutors!!.diskIO.execute {
            skipToItDatabase!!.clearAllTables()

            appExecutors.mainThread.execute {
                listener.onDatabaseCleared()
            }
        }
    }
}