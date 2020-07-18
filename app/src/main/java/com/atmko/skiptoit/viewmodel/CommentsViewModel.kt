package com.atmko.skiptoit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atmko.skiptoit.dependencyinjection.DaggerSkipToItApiComponent
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.SkipToItService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class CommentsViewModel(application: Application): AndroidViewModel(application) {

    @Inject
    lateinit var podcastService: SkipToItService
    private val disposable: CompositeDisposable = CompositeDisposable()

    init {
        DaggerSkipToItApiComponent.create().inject(this)
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(getApplication())
    }

    val isCreated: MutableLiveData<Boolean> = MutableLiveData()
    val createError: MutableLiveData<Boolean> = MutableLiveData()
    val processing: MutableLiveData<Boolean> = MutableLiveData()

    fun createComment(podcastId: String, parentId: String?, episodeId: String, comment: String) {
        getGoogleAccount()?.let { account ->
            account.idToken?.let {
                isCreated.value = false
                processing.value = true
                disposable.add(
                    podcastService.createComment(podcastId, parentId, episodeId, it, comment)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Response<Void>>() {
                            override fun onSuccess(response: Response<Void>) {
                                isCreated.value = response.isSuccessful
                                createError.value = false
                                processing.value = false
                            }

                            override fun onError(e: Throwable?) {
                                createError.value = true
                                processing.value = false
                            }
                        })
                )
            }
        }
    }

    val episodeComments:MutableLiveData<List<Comment>> = MutableLiveData()
    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    fun getComments(podcastId: String, episodeId: String, page: Int) {
        loading.value = true
        disposable.add(
            podcastService.getComments(podcastId, episodeId, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<Comment>>() {
                    override fun onSuccess(comments: List<Comment>?) {
                        episodeComments.value = comments
                        loadError.value = false
                        loading.value = false
                    }

                    override fun onError(e: Throwable?) {
                        loadError.value = true
                        loading.value = false
                    }
                })
        )
    }

    //live data value holds map of type (String : MutableList<Any>)
    //MutableList<Any> holds two items (Comment i.e parentComment and List<Comments> i.e replies)
    val repliesMap: MutableLiveData<HashMap<String, MutableList<Any>>> = MutableLiveData()
    val repliesLoadError: MutableLiveData<Boolean> = MutableLiveData()
    val repliesLoading: MutableLiveData<Boolean> = MutableLiveData()

    //helper method to save parent comment
    fun saveParentComment(comment: Comment) {
        if (repliesMap.value != null) {
            repliesMap.value!![comment.commentId] = mutableListOf(comment, listOf<Comment>())
        } else {
            val map = HashMap<String, MutableList<Any>>()
            map[comment.commentId] = mutableListOf(comment, listOf<Comment>())
            repliesMap.value = map
        }
    }

    //helper method to save replies in repliesMap
    private fun insertReplies(parentId: String, replies: List<Comment>) {
        val currentValue = repliesMap.value
        currentValue!![parentId]!![1] = replies

        repliesMap.value = currentValue
    }

    //helper method to access already saved replies
    @Suppress("UNCHECKED_CAST")
    fun retrieveReplies(commentId: String): List<Comment> {
        return repliesMap.value!![commentId]!![1] as List<Comment>
    }

    //helper method to access already saved parent comment
    @Suppress("UNCHECKED_CAST")
    fun retrieveParentComment(commentId: String): Comment {
        return repliesMap.value!![commentId]!![0] as Comment
    }

    //network call to get comment's replies
    fun getReplies(commentId: String, page: Int) {
        repliesLoading.value = true
        disposable.add(
            podcastService.getReplies(commentId, page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<Comment>>() {
                    override fun onSuccess(replies: List<Comment>) {
                        insertReplies(commentId, replies)
                        repliesLoadError.value = false
                        repliesLoading.value = false
                    }

                    override fun onError(e: Throwable?) {
                        repliesLoadError.value = true
                        repliesLoading.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}