package com.atmko.skiptoit.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.BuildConfig
import com.atmko.skiptoit.dependencyinjection.DaggerListenNotesApiComponent
import com.atmko.skiptoit.model.SkipToItApi
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
import retrofit2.Response
import javax.inject.Inject

const val REQUEST_CODE_SIGN_IN : Int = 547

class MasterActivityViewModel(application: Application): AndroidViewModel(application) {

    val messageEvent = LiveMessageEvent<ViewNavigation>()
    val currentUser: MutableLiveData<User> = MutableLiveData()

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    @Inject
    lateinit var skipToItService: SkipToItApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerListenNotesApiComponent.create().inject(this)
    }

    interface ViewNavigation {
        fun startActivityForResult(intent: Intent, requestCode: Int)
    }

    fun getLastSignedInUser() {
        if (getGoogleAccount() != null) {
            getUser()
            return
        }

        currentUser.value = null
    }

    fun signIn(context: Context) {
        if (getGoogleAccount() != null) {
            getUser()
            return
        }
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
                    account?.let { getUser() }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            } else {
                //todo show user a message
            }
        }
    }

    private fun getUser() {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItService.getUser(it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<User>() {
                            override fun onSuccess(user: User) {
                                currentUser.value = user
                                loadError.value = false
                                loading.value = false
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                            }
                        })
                )
            }
        }
    }

    public fun updateUsername(username: String) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItService.updateUsername(it, username)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object: DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                getUser()
                            }

                            override fun onError(e: Throwable) {
                                loadError.value = true
                                loading.value = false
                            }
                        })
                )
            }
        }
    }

    override fun onCleared() {
        disposable.clear()
    }
}