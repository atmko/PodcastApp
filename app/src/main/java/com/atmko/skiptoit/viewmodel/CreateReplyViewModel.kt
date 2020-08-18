package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class CreateReplyViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val createdReply: MutableLiveData<Comment> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createReply(parentId: String, comment: String) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                processing.value = true
                disposable.add(
                    skipToItApi.createReply(parentId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Comment>() {
                            override fun onSuccess(reply: Comment) {
                                createdReply.value = reply
                                createError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable) {
                                createError.value = true
                                processing.value = false
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