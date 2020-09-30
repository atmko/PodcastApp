package com.atmko.skiptoit.viewmodel

import com.atmko.skiptoit.createreply.CreateReplyEndpoint
import com.atmko.skiptoit.createreply.CreateReplyViewModel
import com.atmko.skiptoit.createreply.ReplyPageTrackerHelper
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.testdata.CommentMocks
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CreateReplyViewModelTest {

    // region constants
    val PARENT_ID: String = "parentId"
    val REPLY_BODY: String = "commentBody"
    // endregion constants

    // end region helper fields
    private lateinit var mCreateReplyEndpointTd: CreateReplyEndpointTd
    lateinit var mReplyPageTrackerHelperTd: ReplyPageTrackerHelperTd

    @Mock
    lateinit var mListenerMock1: CreateReplyViewModel.Listener
    @Mock
    lateinit var mListenerMock2: CreateReplyViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: CreateReplyViewModel

    @Before
    fun setup() {
        mCreateReplyEndpointTd = CreateReplyEndpointTd()
        mReplyPageTrackerHelperTd = ReplyPageTrackerHelperTd()
        SUT = CreateReplyViewModel(mCreateReplyEndpointTd, mReplyPageTrackerHelperTd)
        success()
    }

    @Test
    fun createReplyAndNotify_correctParentIdAndReplyBodyPassedToEndPoint() {
        // Arrange
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mCreateReplyEndpointTd.mParentId, `is`(PARENT_ID))
        assertThat(mCreateReplyEndpointTd.mReplyBody, `is`(REPLY_BODY))
    }

    @Test
    fun createReplyAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).notifyProcessing()
        verify(mListenerMock2).notifyProcessing()
    }

    @Test
    fun createReplyAndNotify_correctReplyPassedToReplyPageTrackerHelper() {
        // Arrange
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        assertThat(mReplyPageTrackerHelperTd.mReply, `is`(CommentMocks.GET_REPLY()))
    }

    @Test
    fun createReplyAndNotify_success_listenersNotifiedWithCorrectReply() {
        // Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        val ac: ArgumentCaptor<Comment> = ArgumentCaptor.forClass(Comment::class.java)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated(ac.capture())
        verify(mListenerMock2).onReplyCreated(ac.capture())
        assertThat(ac.allValues[0], `is`(CommentMocks.GET_REPLY()))
        assertThat(ac.allValues[1], `is`(CommentMocks.GET_REPLY()))
    }

    @Test
    fun createReplyAndNotify_success_unsubscribedObserversNotNotified() {
        //Arrange
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        SUT.unregisterListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreated(ArgumentMatchers.any())
        verify(mListenerMock2, never()).onReplyCreated(ArgumentMatchers.any())
    }

    @Test
    fun createReplyAndNotify_success_observersNotifiedOfFailure() {
        //Arrange
        failure()
        SUT.registerListener(mListenerMock1)
        SUT.registerListener(mListenerMock2)
        // Act
        SUT.createReplyAndNotify(PARENT_ID, REPLY_BODY)
        // Assert
        verify(mListenerMock1).onReplyCreateFailed()
        verify(mListenerMock2).onReplyCreateFailed()
    }

    // region helper methods
    private fun success() {
        // no-op because mFailure false by default
    }

    // region helper methods
    private fun failure() {
        mCreateReplyEndpointTd.mFailure = true
    }
    // endregion helper methods

    // region helper classes
    class CreateReplyEndpointTd : CreateReplyEndpoint(null, null) {

        var mParentId: String = ""
        var mReplyBody: String = ""
        var mFailure = false

        override fun createReply(
            parentId: String,
            replyBody: String,
            listener: Listener
        ) {
            mParentId = parentId
            mReplyBody = replyBody
            if (!mFailure) {
                listener.onCreateSuccess(CommentMocks.GET_REPLY())
            } else {
                listener.onCreateFailed()
            }
        }
    }

    class ReplyPageTrackerHelperTd : ReplyPageTrackerHelper(null, null) {

        var mReply: Comment? = null
        var mFailure = false

        override fun updatePagingTracker(reply: Comment, listener: Listener) {
            mReply = reply
            if (!mFailure) {
                listener.onPagingDataUpdated(reply)
            }
        }
    }
    // endregion helper classes
}