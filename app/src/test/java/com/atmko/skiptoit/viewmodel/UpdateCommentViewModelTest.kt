package com.atmko.skiptoit.viewmodel

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentCache
import com.atmko.skiptoit.updatecomment.UpdateCommentEndpoint
import com.atmko.skiptoit.updatecomment.UpdateCommentViewModel
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException

@RunWith(MockitoJUnitRunner::class)
class UpdateCommentViewModelTest {

    // region constants
    companion object {
        val COMMENT_ID_1: String = "commentId1"
        val COMMENT_ID_2: String = "commentId2"
        val PARENT_ID: String = "parentId"
        val EPISODE_ID: String = "episodeId"
        val USERNAME: String = "username"
        val BODY: String = "body"
        val VOTE_TALLY: Int = 0
        val IS_USER_COMMENT: Boolean = false
        val VOTE_WEIGHT: Int = 0
        val REPLIES: Int = 0
        val TIMESTAMP: Long = 1000000
        val PROFILE_IMAGE: String = "profileImage"

        val BODY_UPDATE: String = "bodyUpdate"

        private fun GET_COMMENT_1(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        private fun GET_COMMENT_2(): Comment {
            return Comment(
                COMMENT_ID_2,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }

        private fun GET_UPDATED_COMMENT(): Comment {
            return Comment(
                COMMENT_ID_1,
                PARENT_ID,
                EPISODE_ID,
                USERNAME,
                BODY_UPDATE,
                VOTE_TALLY,
                IS_USER_COMMENT,
                VOTE_WEIGHT,
                REPLIES ,
                TIMESTAMP ,
                PROFILE_IMAGE
            )
        }
    }
    // endregion constants

    // end region helper fields
    private lateinit var mUpdateCommentEndpointTd: UpdateCommentEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    private lateinit var mListenerTd1: ListenerTd
    private lateinit var mListenerTd2: ListenerTd
    // endregion helper fields

    private lateinit var SUT: UpdateCommentViewModel

    @Before
    fun setup() {
        mUpdateCommentEndpointTd = UpdateCommentEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = UpdateCommentViewModel(
            mUpdateCommentEndpointTd,
            mCommentCacheTd
        )
        mListenerTd1 = ListenerTd()
        mListenerTd2 = ListenerTd()
        loadCommentSuccess()
        updateCommentSuccess()
    }

    @Test
    fun getCachedCommentAndNotify_correctCommentIdPassedToMethod() {
        // Arrange
        // Act
        SUT.getCachedCommentAndNotify(COMMENT_ID_1)
        // Assert
        assertThat(mCommentCacheTd.mPassedCommentId, `is`(COMMENT_ID_1))
    }

    @Test
    fun getCachedCommentAndNotify_loadCommentSuccess_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.getCachedCommentAndNotify(COMMENT_ID_1)
        // Assert
        mListenerTd1.assertNotifyProcessingCalled(1)
        mListenerTd2.assertNotifyProcessingCalled(1)
    }

    @Test
    fun getCachedCommentAndNotify_loadCommentSuccess_listenersNotifiedOfSuccess() {
        // Arrange
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.getCachedCommentAndNotify(COMMENT_ID_1)
        // Assert
        mListenerTd1.assertOnLoadCommentCalled(1)
        mListenerTd2.assertOnLoadCommentCalled(1)
    }

    @Test
    fun getCachedCommentAndNotify_loadCommentSuccess_commentSavedInViewModel() {
        // Arrange
        SUT.registerListener(mListenerTd1)
        // Act
        SUT.getCachedCommentAndNotify(COMMENT_ID_1)
        // Assert
        mListenerTd1.assertOnLoadCommentCalled(1)
        assertThat(SUT.comment, `is`(GET_COMMENT_1()))
    }

    @Test
    fun getCachedCommentAndNotify_loadCommentSuccess_secondCallReturnsFromSavedValue() {
        // Arrange
        SUT.registerListener(mListenerTd1)
        // Act
        SUT.getCachedCommentAndNotify(COMMENT_ID_1)
        SUT.getCachedCommentAndNotify(COMMENT_ID_2)
        // Assert
        assertThat(mCommentCacheTd.mInvocationCounter, `is`(1))
        assertThat(SUT.comment, `is`(GET_COMMENT_1()))
    }

    // todo test load comment - fail

    //--------------------------------------------

    @Test
    fun updateCommentBodyAndNotify_updateCommentSuccess_updatedCorrectBodySentByListeners() {
        //Arrange
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        mListenerTd1.assertOnCommentUpdatedCalled(1)
        assertThat(SUT.comment, `is`(GET_UPDATED_COMMENT()))
    }

    @Test
    fun updateCommentBodyAndNotify_updateCommentFailure_notifyListenersOfFailure() {
        //Arrange
        updateCommentFailure()
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        mListenerTd1.assertOnCommentUpdateFailedCalled(1)
        mListenerTd2.assertOnCommentUpdateFailedCalled(1)
    }

    @Test
    fun updateCommentBodyAndNotify_updateCommentSuccess_updatedCommentCached() {
        //Arrange
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        assertThat(mCommentCacheTd.mCacheCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdatedComment, `is`(GET_UPDATED_COMMENT()))
    }

    @Test
    fun updateCommentBodyAndNotify_updateCommentSuccess_unsubscribedObserversNotNotified() {
        //Arrange
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        SUT.unregisterListener(mListenerTd2)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        mListenerTd1.assertOnCommentUpdatedCalled(1)
        mListenerTd2.assertOnCommentUpdatedCalled(0)
    }

    // region helper methods
    private fun loadCommentSuccess() {
        // no-op because mFailure false by default
    }

    // region helper methods
    private fun updateCommentSuccess() {
        // no-op because mFailure false by default
    }

    private fun updateCommentFailure() {
        mUpdateCommentEndpointTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CommentCacheTd : CommentCache(null) {

        var mPassedCommentId: String = ""
        var mInvocationCounter = 0
        var mFailure = false

        override
        fun getCachedComment (commentId: String, listener: CommentFetchListener) {
            mPassedCommentId = commentId
            mInvocationCounter += 1
            if (!mFailure) {
                if (mInvocationCounter == 1) {
                    listener.onCommentFetchSuccess(GET_COMMENT_1())
                } else if (mInvocationCounter == 2) {
                    listener.onCommentFetchSuccess(GET_COMMENT_2())
                }
            }
        }

        var mCacheCounter = 0
        var mUpdatedComment: Comment = GET_COMMENT_1()

        override fun updateLocalCache(updatedComment: Comment, listener: CacheUpdateListener) {
            mCacheCounter += 1
            mUpdatedComment = updatedComment
            listener.onLocalCacheUpdateSuccess()
        }
    }

    class UpdateCommentEndpointTd: UpdateCommentEndpoint(null, null) {

        lateinit var mPassedComment: Comment
        var mPassedBodyUpdate: String = ""
        var mFailure = false

        override fun updateComment(currentComment: Comment, bodyUpdate: String, listener: Listener) {
            mPassedComment = currentComment
            mPassedBodyUpdate = bodyUpdate
            if (!mFailure) {
                listener.onUpdateSuccess(BODY_UPDATE)
            } else {
                listener.onUpdateFailed()
            }
        }
    }

    class ListenerTd : UpdateCommentViewModel.Listener {

        private var mNotifyProcessingCounter = 0
        override fun notifyProcessing() {
            mNotifyProcessingCounter++
        }

        fun assertNotifyProcessingCalled(times: Int) {
            if (times != mNotifyProcessingCounter) {
                throw RuntimeException("Method called:$mNotifyProcessingCounter instead of:$times")
            }
        }

        private var mOnLoadCommentCounter = 0
        override fun onLoadComment(fetchedComment: Comment) {
            mOnLoadCommentCounter++
        }

        fun assertOnLoadCommentCalled(times: Int) {
            if (times != mOnLoadCommentCounter) {
                throw RuntimeException("Method called:$mOnLoadCommentCounter instead of:$times")
            }
        }

        private var mOnLoadCommentFailureCounter = 0
        override fun onLoadCommentFailed() {
            mOnLoadCommentFailureCounter++
        }

        fun assertOnLoadCommentFailureCalled(times: Int) {
            if (times != mOnLoadCommentFailureCounter) {
                throw RuntimeException("Method called:$mOnLoadCommentFailureCounter instead of:$times")
            }
        }

        private var mOnCommentUpdatedCounter = 0
        override fun onCommentUpdated() {
            mOnCommentUpdatedCounter++
        }

        fun assertOnCommentUpdatedCalled(times: Int) {
            if (times != mOnCommentUpdatedCounter) {
                throw RuntimeException("Method called:$mOnCommentUpdatedCounter instead of:$times")
            }
        }

        private var mOnCommentUpdateFailedCounter = 0
        override fun onCommentUpdateFailed() {
            mOnCommentUpdateFailedCounter++
        }

        fun assertOnCommentUpdateFailedCalled(times: Int) {
            if (times != mOnCommentUpdateFailedCounter) {
                throw RuntimeException("Method called:$mOnCommentUpdateFailedCounter instead of:$times")
            }
        }
    }
    // endregion helper classes

}