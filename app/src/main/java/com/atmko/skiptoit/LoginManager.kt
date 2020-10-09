package com.atmko.skiptoit

import android.content.Intent
import android.content.SharedPreferences
import com.atmko.skiptoit.launch.IS_FIRST_SETUP_KEY
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

open class LoginManager(
    private val googleSignInClient: GoogleSignInClient?,
    private val sharedPreferences: SharedPreferences?
) {

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

    open fun signOut(listener: SignOutListener) {
        googleSignInClient!!.signOut().addOnSuccessListener {
            listener.onSignOutSuccess()
        }.addOnFailureListener {
            listener.onSignOutFailed()
        }
    }

    open fun isFirstSetUp(): Boolean {
        return sharedPreferences!!.getBoolean(IS_FIRST_SETUP_KEY, true)
    }

    open fun setIsFirstSetup(isFirstSetup: Boolean) {
        sharedPreferences!!.edit().putBoolean(IS_FIRST_SETUP_KEY, isFirstSetup).apply()
    }
}