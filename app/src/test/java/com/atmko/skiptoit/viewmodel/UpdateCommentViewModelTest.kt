package com.atmko.skiptoit.viewmodel

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks.Companion.BODY_UPDATE
import com.atmko.skiptoit.testdata.CommentMocks.Companion.GET_COMMENT_1
import com.atmko.skiptoit.testdata.CommentMocks.Companion.GET_COMMENT_WITH_UPDATED_BODY
import com.atmko.skiptoit.updatecomment.UpdateCommentEndpoint
import com.atmko.skiptoit.updatecomment.UpdateCommentViewModel
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdateCommentViewModelTest {

    // region constants
    val COMMENT_ID_1: String = "commentId1"
    val COMMENT_ID_2: String = "commentId2"
    val BODY_UPDATE: String = "bodyUpdate"

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
        assertThat(mCommentCacheTd.mCommentId, `is`(COMMENT_ID_1))
    }

    @Test
    fun getCachedCommentAndNotify_listenersNotifiedOfProcessing() {
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
        assertThat(mCommentCacheTd.mGetCachedCommentCounter, `is`(1))
        assertThat(SUT.comment, `is`(GET_COMMENT_1()))
    }

    // todo test load comment - fail

    //--------------------------------------------

    @Test
    fun updateCommentBodyAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        mListenerTd1.assertNotifyProcessingCalled(1)
        mListenerTd2.assertNotifyProcessingCalled(1)
    }

    @Test
    fun updateCommentBodyAndNotify_updateCommentSuccess_listenersReceiveCorrectUpdatedBody() {
        //Arrange
        SUT.comment = GET_COMMENT_1()
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.updateCommentBodyAndNotify(BODY_UPDATE)
        // Assert
        mListenerTd1.assertOnCommentUpdatedCalled(1)
        mListenerTd2.assertOnCommentUpdatedCalled(1)
        assertThat(SUT.comment, `is`(GET_COMMENT_WITH_UPDATED_BODY()))
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
        assertThat(mCommentCacheTd.mUpdateLocalCacheCounter, `is`(1))
        assertThat(mCommentCacheTd.mUpdatedComment, `is`(GET_COMMENT_WITH_UPDATED_BODY()))
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

    private fun updateCommentSuccess() {
        // no-op because mFailure false by default
    }

    private fun updateCommentFailure() {
        mUpdateCommentEndpointTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class UpdateCommentEndpointTd: UpdateCommentEndpoint(null, null) {

        private lateinit var mComment: Comment
        var mBodyUpdate: String = ""
        var mFailure = false

        override fun updateComment(currentComment: Comment, bodyUpdate: String, listener: Listener) {
            mComment = currentComment
            mBodyUpdate = bodyUpdate
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

        private var mOnLoadCommentFailedCounter = 0
        override fun onLoadCommentFailed() {
            mOnLoadCommentFailedCounter++
        }

        fun assertOnLoadCommentFailedCalled(times: Int) {
            if (times != mOnLoadCommentFailedCounter) {
                throw RuntimeException("Method called:$mOnLoadCommentFailedCounter instead of:$times")
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