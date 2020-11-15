package com.atmko.skiptoit.testclass

import android.content.Intent
import com.atmko.skiptoit.LoginManager
import com.atmko.skiptoit.model.database.SubscriptionsCache
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class LoginManagerTd : LoginManager(null, null, null, null) {

    var mSilentSignInCounter = 0
    var mSilentSignInFailure = false
    lateinit var mGoogleSignInAccount: GoogleSignInAccount
    override fun silentSignIn(listener: SignInListener) {
        mSilentSignInCounter += 1
        if (!mSilentSignInFailure) {
            listener.onSignInSuccess(mGoogleSignInAccount)
        } else {
            listener.onSignInFailed(Intent())
        }
    }

    var mGetSignedInAccountCounter = 0
    var mGetSignedInAccountFailure = false
    lateinit var mIntent: Intent
    override fun getSignedInAccount(intent: Intent, listener: GoogleAccountFetchListener) {
        mGetSignedInAccountCounter += 1
        mIntent = intent
        if (!mGetSignedInAccountFailure) {
            listener.onAccountFetchSuccess()
        } else {
            listener.onAccountFetchFailed()
        }
    }

    var mSetSubscriptionsSyncedCounter = 0
    var mSetSubscriptionsSyncedArgIsSubscriptionSynced: Boolean = true
    override fun setSubscriptionsSynced(
        isSubscriptionsSynced: Boolean,
        listener: SyncStatusUpdateListener
    ) {
        mSetSubscriptionsSyncedCounter += 1
        mSetSubscriptionsSyncedArgIsSubscriptionSynced = isSubscriptionsSynced
        listener.onSyncStatusUpdated()
    }

    var mIsSubscriptionsSyncedCounter = 0
    override fun isSubscriptionsSynced(listener: SubscriptionsCache.SyncStatusFetchListener) {
        mIsSubscriptionsSyncedCounter += 1
        listener.onSyncStatusFetched(mSetSubscriptionsSyncedArgIsSubscriptionSynced)
    }

    var mSignOutCounter = 0
    var mSignOutFailure = false
    override fun signOut(listener: SignOutListener) {
        mSignOutCounter += 1
        if (!mSignOutFailure) {
            listener.onSignOutSuccess()
        } else {
            listener.onSignOutFailed()
        }
    }

    var mIsFirstSetupCounter = 0
    var mIsFirstSetUpError = false
    override fun isFirstSetUp(): Boolean {
        mIsFirstSetupCounter += 1
        return !mIsFirstSetUpError
    }

    var mSetIsFirstSetupCounter = 0
    var mIsFirstSetup: Boolean? = null
    override fun setIsFirstSetup(isFirstSetup: Boolean) {
        mSetIsFirstSetupCounter += 1
        mIsFirstSetup = isFirstSetup
    }

    var mClearDatabaseCounter = 0
    var mClearDatabaseFailure = false
    override fun clearDatabase(listener: ClearDatabaseListener) {
        mClearDatabaseCounter += 1
        if (!mClearDatabaseFailure) {
            listener.onDatabaseCleared()
        } else {
            listener.onDatabaseClearFailed()
        }
    }
}
