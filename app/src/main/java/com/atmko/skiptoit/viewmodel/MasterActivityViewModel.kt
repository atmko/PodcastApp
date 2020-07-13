package com.atmko.skiptoit.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.BuildConfig
import com.atmko.skiptoit.dependencyinjection.DaggerSkipToItApiComponent
import com.atmko.skiptoit.model.SkipToItService
import com.atmko.skiptoit.model.Token
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.viewmodel.livedataextensions.LiveMessageEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

const val REQUEST_CODE_SIGN_IN : Int = 547

class MasterActivityViewModel(application: Application): AndroidViewModel(application) {

    val messageEvent = LiveMessageEvent<ViewNavigation>()
    val currentUser: MutableLiveData<User> = MutableLiveData()

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var skipToItService: SkipToItService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    interface ViewNavigation {
        fun startActivityForResult(intent: Intent, requestCode: Int)
    }

    fun signIn(context: Context) {
        val options: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.googleServerId)
                .requestEmail()
                .build()

        val client = GoogleSignIn.getClient(context, options)
        val intent = client.signInIntent
        messageEvent.sendEvent { startActivityForResult(intent, REQUEST_CODE_SIGN_IN) }
    }

    fun signOut(context: Context) {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        GoogleSignIn.getClient(context, gso).signOut()
        currentUser.value = null
    }

    fun isSignedIn(): Boolean {
        return currentUser.value != null && isGoogleSignedIn()
    }

    private fun isGoogleSignedIn(): Boolean {
        return getGoogleAccount() != null
    }

    private fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(getApplication())
    }

    fun onRequestResultReceived(requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account: GoogleSignInAccount? = task.result
                    account?.let { handleSignInResult(it) }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            } else {
                //todo show user a message
            }
        }
    }

    private fun handleSignInResult(googleSignInAccount: GoogleSignInAccount) {
        googleSignInAccount.let { account ->
            account.idToken?.let {
                getUser(Token(it))
            }
        }
    }

    private fun getUser(token: Token) {
        loading.value = true
        disposable.add(
            skipToItService.getUser(token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<User>() {
                    override fun onSuccess(user: User?) {
                        currentUser.value = user
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadError.value = true
                        loading.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}