package com.atmko.skiptoit.testclass

import android.content.Intent
import com.atmko.skiptoit.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class LoginManagerTd : LoginManager(null, null) {

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
    override fun isFirstSetUp(): Boolean {
        mIsFirstSetupCounter += 1
        return true
    }

    var mSetIsFirstSetupCounter = 0
    var mIsFirstSetup: Boolean? = null
    override fun setIsFirstSetup(isFirstSetup: Boolean) {
        mSetIsFirstSetupCounter += 1
        mIsFirstSetup = isFirstSetup
    }
}
