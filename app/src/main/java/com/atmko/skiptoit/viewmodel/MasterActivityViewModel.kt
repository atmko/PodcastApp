package com.atmko.skiptoit.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.BuildConfig
import com.atmko.skiptoit.dependencyinjection.application.DaggerApplicationComponent
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.atmko.skiptoit.viewmodel.livedataextensions.LiveMessageEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
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
    @Inject
    lateinit var podcastService: PodcastsApi
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerApplicationComponent.create().inject(this)
    }

    interface ViewNavigation {
        fun startActivityForResult(intent: Intent, requestCode: Int)
    }

    fun signIn(context: Context) {
        if (getGoogleAccount() != null) {
            getUser()
            return
        }

        val client = getSignInClient(context)
        val intent = client.signInIntent
        messageEvent.sendEvent { startActivityForResult(intent, REQUEST_CODE_SIGN_IN) }
    }

    private fun getSignInClient(context: Context): GoogleSignInClient {
        val options: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.googleServerId)
                .requestEmail()
                .build()

        return GoogleSignIn.getClient(context, options)
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
                    account?.let {
                        getUser()
                        restoreLocalData()
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            } else {
                //todo show user a message
            }
        }
    }

    fun getUser() {
        getSignInClient(getApplication()).silentSignIn().addOnSuccessListener { account ->
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
        }.addOnFailureListener {
            //todo show user a message
        }
    }

    public fun updateUsername(username: String) {
        getSignInClient(getApplication()).silentSignIn().addOnSuccessListener { account ->
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
        }.addOnFailureListener {
            signIn(getApplication())
        }
    }

    private fun restoreLocalData() {
        getRemoteSubscriptions()
    }

    val remoteFetchError: MutableLiveData<Boolean> = MutableLiveData()
    val remoteFetching: MutableLiveData<Boolean> = MutableLiveData()

    fun getRemoteSubscriptions() {
        getSignInClient(getApplication()).silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                remoteFetching.value = true
                disposable.add(
                    skipToItService.getSubscriptions(it)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Subscription>>() {
                            override fun onSuccess(remoteSubscriptions: List<Subscription>) {
                                getBatchPodcastData(remoteSubscriptions)
                                remoteFetchError.value = false
                                remoteFetching.value = false
                            }

                            override fun onError(e: Throwable) {
                                remoteFetchError.value = true
                                remoteFetching.value = false
                            }
                        })
                )
            }
        }.addOnFailureListener {
            signIn(getApplication())
        }
    }

    val batchFetchError: MutableLiveData<Boolean> = MutableLiveData()
    val batchFetching: MutableLiveData<Boolean> = MutableLiveData()

    private fun getBatchPodcastData(subscriptions: List<Subscription>) {
        batchFetching.value = true
        disposable.add(
            podcastService.getBatchPodcastMetadata(combinePodcastIds(subscriptions))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ApiResults>() {
                    override fun onSuccess(results: ApiResults) {
                        saveToLocalDatabase(results.podcasts!!)
                        batchFetchError.value = false
                        batchFetching.value = false
                    }

                    override fun onError(e: Throwable) {
                        batchFetchError.value = true
                        batchFetching.value = false
                    }
                })
        )
    }

    val batchLocalSaveError: MutableLiveData<Boolean> = MutableLiveData()
    val batchSavingLocally: MutableLiveData<Boolean> = MutableLiveData()

    private fun saveToLocalDatabase(podcasts: List<Podcast>) {
        val skipToItDatabase = SkipToItDatabase.getInstance(getApplication())
        AppExecutors.getInstance().diskIO().execute(Runnable {
            for (podcast in podcasts) {
                skipToItDatabase.subscriptionsDao().createSubscription(podcast)
            }

            AppExecutors.getInstance().mainThread().execute(Runnable {
                batchLocalSaveError.value = false
                batchSavingLocally.value = false
            })
        })
    }

    private fun combinePodcastIds(subscriptions: List<Subscription>): String {
        val builder: StringBuilder = java.lang.StringBuilder()
        var counter = 0
        while (counter < subscriptions.size) {
            builder.append(subscriptions[counter].listenNotesId)
            if (counter != subscriptions.size - 1) {
                builder.append(",")
            }
            counter += 1
        }

        return builder.toString()
    }

    override fun onCleared() {
        disposable.clear()
    }
}