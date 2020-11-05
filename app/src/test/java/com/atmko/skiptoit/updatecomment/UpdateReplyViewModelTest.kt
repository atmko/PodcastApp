package com.atmko.skiptoit.updatecomment

import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks.Companion.GET_COMMENT_1
import com.atmko.skiptoit.testutils.kotlinCapture
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdateReplyViewModelTest {

    // region constants
    val COMMENT_ID_1: String = "commentId1"
    val COMMENT_ID_2: String = "commentId2"

    // endregion constants

    // end region helper fields
    private lateinit var mUpdateCommentEndpointTd: UpdateCommentViewModelTest.UpdateCommentEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock lateinit var mListenerTd1: UpdateCommentViewModel.Listener
    @Mock lateinit var mListenerTd2: UpdateCommentViewModel.Listener

    @Mock lateinit var mRepliesViewModelListenerMock1: UpdateReplyViewModel.Listener
    @Mock lateinit var mRepliesViewModelListenerMock2: UpdateReplyViewModel.Listener
    // endregion helper fields

    private lateinit var SUT: UpdateReplyViewModel

    @Before
    fun setup() {
        mUpdateCommentEndpointTd =
            UpdateCommentViewModelTest.UpdateCommentEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = UpdateReplyViewModel(
            mUpdateCommentEndpointTd,
            mCommentCacheTd
        )

        loadCommentSuccess()
        updateCommentSuccess()
    }

    @Test
    fun getCachedParentCommentAndNotify_correctCommentIdPassedToMethod() {
        // Arrange
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        assertThat(mCommentCacheTd.mCommentId, `is`(COMMENT_ID_1))
    }

    @Test
    fun getCachedParentCommentAndNotify_listenersNotifiedOfProcessing() {
        // Arrange
        SUT.registerListener(mListenerTd1)
        SUT.registerListener(mListenerTd2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mListenerTd1).notifyProcessing()
        verify(mListenerTd2).notifyProcessing()
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNullCommentReturned_listenersNotifiedOfError() {
        // Arrange
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock1)
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mRepliesViewModelListenerMock1).onLoadParentCommentFailed()
        verify(mRepliesViewModelListenerMock2).onLoadParentCommentFailed()
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNonNullCommentReturned_commentSavedInViewModel() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        SUT.registerListener(mListenerTd1)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        assertThat(SUT.parentComment, `is`(GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_getCachedCommentSuccessNonNullCommentReturned_listenersNotifiedOfSuccess() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        val ac: ArgumentCaptor<Comment> = ArgumentCaptor.forClass(Comment::class.java)
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock1)
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mRepliesViewModelListenerMock1).onLoadParentComment(ac.kotlinCapture())
        verify(mRepliesViewModelListenerMock2).onLoadParentComment(ac.kotlinCapture())
        assertThat(ac.allValues[0], `is`(GET_COMMENT_1()))
        assertThat(ac.allValues[1], `is`(GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_loadCommentSuccessNonNullCommentReturned_secondCallReturnsFromSavedValue() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_2)
        // Assert
        assertThat(mCommentCacheTd.mGetCachedCommentCounter, `is`(1))
        assertThat(SUT.parentComment, `is`(GET_COMMENT_1()))
    }

    @Test
    fun getCachedParentCommentAndNotify_loadCommentSuccessNonNullCommentReturned_listenersNotifiedOfError() {
        // Arrange
        getCachedCommentFailure()
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock1)
        SUT.registerReplyViewModelListener(mRepliesViewModelListenerMock2)
        // Act
        SUT.getCachedParentCommentAndNotify(COMMENT_ID_1)
        // Assert
        verify(mRepliesViewModelListenerMock1).onLoadParentCommentFailed()
        verify(mRepliesViewModelListenerMock2).onLoadParentCommentFailed()
    }

    // region helper methods
    private fun loadCommentSuccess() {
        // no-op because mFailure false by default
    }

    private fun updateCommentSuccess() {
        // no-op because mFailure false by default
    }

    fun getCachedCommentNonNullCommentReturned() {
        mCommentCacheTd.mGetCachedCommentNullCommentReturned = false
    }

    fun getCachedCommentFailure() {
        mCommentCacheTd.mGetCachedCommentFailure = true
    }
    // endregion helper methods

    // region helper classes

    // endregion helper classes

}