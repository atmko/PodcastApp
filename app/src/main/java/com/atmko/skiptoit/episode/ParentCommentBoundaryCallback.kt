package com.atmko.skiptoit.episode

import android.util.Log
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.model.database.SkipToItDatabase
import com.atmko.skiptoit.util.AppExecutors
import com.atmko.skiptoit.episode.common.CommentBoundaryCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Tasks

class ParentCommentBoundaryCallback(
    private val googleSignInClient: GoogleSignInClient,
    private val skipToItApi: SkipToItApi,
    private val skipToItDatabase: SkipToItDatabase,
    listener: Listener
): CommentBoundaryCallback(skipToItDatabase, listener) {

    private val tag = this::class.simpleName

    override fun onZeroItemsLoaded() {
        Log.d("REFRESHING","REFRESHING")
        listener.onPageLoading()
        AppExecutors.getInstance().diskIO().execute {
            requestPage(loadTypeRefresh, startPage)
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Comment) {
        Log.d(tag,"APPEND")
        listener.onPageLoading()
        AppExecutors.getInstance().diskIO().execute {
            val bottomItemTracker: CommentPageTracker = getCommentPageTracker(itemAtEnd.commentId)
            val nextPage = bottomItemTracker.nextPage

            if (nextPage != null) {
                requestPage(loadTypeAppend, nextPage)
            } else {
                notifyOnPageLoad(listener)
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Comment) {
        Log.d(tag,"PREPEND")
        listener.onPageLoading()
        AppExecutors.getInstance().diskIO().execute {
            val topItemTracker: CommentPageTracker = getCommentPageTracker(itemAtFront.commentId)
            val prevPage = topItemTracker.prevPage

            if (prevPage != null) {
                requestPage(loadTypePrepend, prevPage)
            } else {
                notifyOnPageLoad(listener)
            }
        }
    }

    private fun requestPage(requestType: Int, loadKey: Int) {
        val googleSignInAccount = Tasks.await(googleSignInClient.silentSignIn())
        val commentResultCall =
            if (googleSignInAccount != null && googleSignInAccount.idToken != null) {
                skipToItApi.getCommentsAuthenticated(
                    param,
                    loadKey,
                    googleSignInAccount.idToken!!
                )
            } else {
                skipToItApi.getCommentsUnauthenticated(
                    param,
                    loadKey
                )
            }

        val response = commentResultCall.execute()
        if (response.isSuccessful) {
            Log.d(tag, "isSuccessful")
            notifyOnPageLoad(listener)

            val body: CommentResults = response.body()!!
            onCommentFetchCallback(body, requestType)
        } else {
            Log.d(tag, "!isSuccessful")
            notifyOnPageLoadFailed(listener)
        }
    }

    private fun onCommentFetchCallback(
        commentResults: CommentResults,
        loadType: Int
    ): CommentResults {
        skipToItDatabase.beginTransaction()
        try {
            if (loadType == loadTypeRefresh) {
                skipToItDatabase.commentDao().deleteAllComments()
            }

            val comments = commentResults.comments
            val prevPage = if (!commentResults.hasPrev) null else commentResults.page - 1
            val nextPage = if (!commentResults.hasNext) null else commentResults.page + 1
            val pageTrackers = mutableListOf<CommentPageTracker>()
            for (i in comments) {
                pageTrackers.add(
                    CommentPageTracker(
                        i.commentId,
                        commentResults.page,
                        nextPage,
                        prevPage
                    )
                )
            }

            skipToItDatabase.commentDao().insertComments(commentResults.comments)
            skipToItDatabase.commentPageTrackerDao().insert(pageTrackers)
            skipToItDatabase.setTransactionSuccessful()
        } finally {
            skipToItDatabase.endTransaction()
        }
        return commentResults
    }
}