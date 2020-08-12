package com.atmko.skiptoit.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SubscriptionsDao
import com.atmko.skiptoit.util.AppExecutors
import com.atmko.skiptoit.viewmodel.livedataextensions.LiveMessageEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

const val REQUEST_CODE_SIGN_IN : Int = 547

class MasterActivityViewModel(private val skipToItApi: SkipToItApi,
                              private val podcastsApi: PodcastsApi,
                              private val subscriptionsDao: SubscriptionsDao,
                              private val googleSignInClient: GoogleSignInClient,
                              private val application: SkipToItApplication): ViewModel() {

    val messageEvent = LiveMessageEvent<ViewNavigation>()
    val currentUser: MutableLiveData<User> = MutableLiveData()

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val disposable: CompositeDisposable = CompositeDisposable()

    interface ViewNavigation {
        fun startActivityForResult(intent: Intent, requestCode: Int)
    }

    fun signIn() {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account?.let { getUser() }
        }.addOnFailureListener {
            val intent = googleSignInClient.signInIntent
            messageEvent.sendEvent { startActivityForResult(intent, REQUEST_CODE_SIGN_IN) }
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
        currentUser.value = null
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
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItApi.getUser(it)
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

    fun updateUsername(username: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                loading.value = true
                disposable.add(
                    skipToItApi.updateUsername(it, username)
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
            signIn()
        }
    }

    private fun restoreLocalData() {
        getRemoteSubscriptions()
    }

    val remoteFetchError: MutableLiveData<Boolean> = MutableLiveData()
    val remoteFetching: MutableLiveData<Boolean> = MutableLiveData()

    fun getRemoteSubscriptions() {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                remoteFetching.value = true
                disposable.add(
                    skipToItApi.getSubscriptions(it)
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
            signIn()
        }
    }

    val batchFetchError: MutableLiveData<Boolean> = MutableLiveData()
    val batchFetching: MutableLiveData<Boolean> = MutableLiveData()

    private fun getBatchPodcastData(subscriptions: List<Subscription>) {
        batchFetching.value = true
        disposable.add(
            podcastsApi.getBatchPodcastMetadata(combinePodcastIds(subscriptions))
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
        AppExecutors.getInstance().diskIO().execute(Runnable {
            for (podcast in podcasts) {
                subscriptionsDao.createSubscription(podcast)
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