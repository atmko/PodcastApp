package com.atmko.skiptoit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItApi
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.util.AppExecutors
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class UpdateCommentViewModel(
    private val skipToItApi: SkipToItApi,
    private val googleSignInClient: GoogleSignInClient,
    private val commentDao: CommentDao
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    var commentLiveData: LiveData<Comment> = MutableLiveData()

    val isUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val updateError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun loadComment(commentId: String) {
        if (commentLiveData.value != null) {
            return
        }

        processing.value = true
        commentLiveData = commentDao.getComment(commentId)
    }

    fun updateCommentBody(updatedBody: String?) {
        if (commentLiveData.value == null || updatedBody == null) {
            return
        }

        val updatedComment: Comment = commentLiveData.value!!
        updatedComment.body = updatedBody

        googleSignInClient.silentSignIn().addOnSuccessListener { account ->
            account.idToken?.let {
                disposable.add(
                    skipToItApi.updateCommentBody(updatedComment.commentId, it, updatedComment.body)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                if (response.isSuccessful) {
                                    AppExecutors.getInstance().diskIO().execute {
                                        commentDao.updateComment(updatedComment)
                                        isUpdated.postValue(true)

                                        updateError.postValue(false)
                                        processing.postValue(false)
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                updateError.postValue(true)
                                processing.postValue(false)
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