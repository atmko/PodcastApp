package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class UpdateCommentViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val bodyUpdateLiveData: MutableLiveData<BodyUpdate> = MutableLiveData()
    val updateError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun updateCommentBody(commentId: String, bodyUpdate: BodyUpdate) {
        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.updateCommentBody(commentId, it, bodyUpdate.body)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    bodyUpdateLiveData.value = bodyUpdate
                                }
                                updateError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable) {
                                updateError.value = true
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