package com.atmko.skiptoit.episode.replies

import androidx.paging.PagedList
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.episode.common.CommentsViewModelTest
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.database.CommentDao
import com.atmko.skiptoit.testclass.CommentCacheTd
import com.atmko.skiptoit.testdata.CommentMocks
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
class RepliesViewModelTest {

    // region constants

    // endregion constants

    // end region helper fields
    lateinit var replyCommentBoundaryCallback: ReplyCommentBoundaryCallback
    lateinit var mPagedListConfig: PagedList.Config

    private lateinit var mCommentEndpointTd: CommentsViewModelTest.CommentsEndpointTd
    private lateinit var mCommentCacheTd: CommentCacheTd

    @Mock lateinit var mCommentDaoMock: CommentDao
    @Mock lateinit var mListener1: RepliesViewModel.Listener
    @Mock lateinit var mListener2: RepliesViewModel.Listener
    // endregion helper fields

    lateinit var SUT: RepliesViewModel

    @Before
    fun setup() {
        replyCommentBoundaryCallback =
            ReplyCommentBoundaryCallback(
                ReplyCommentBoundaryCallbackTest.GetRepliesEndpointTd(),
                CommentCacheTd()
            )

        mPagedListConfig = PagedList.Config.Builder()
            .setPageSize(CommentsViewModel.pageSize)
            .setPrefetchDistance(CommentsViewModel.prefetchDistance)
            .setEnablePlaceholders(CommentsViewModel.enablePlaceholders)
            .setInitialLoadSizeHint(CommentsViewModel.initialLoadSize)
            .setMaxSize(CommentsViewModel.maxSize)
            .build()

        mCommentEndpointTd = CommentsViewModelTest.CommentsEndpointTd()
        mCommentCacheTd = CommentCacheTd()
        SUT = RepliesViewModel(
            mCommentEndpointTd,
            mCommentCacheTd,
            mCommentDaoMock,
            replyCommentBoundaryCallback,
            mPagedListConfig
        )
    }

    @Test
    fun getParentCommentAndNotify_parentIdSavedToViewModelVariable() {
        // Arrange
        // Act
        SUT.getParentCommentAndNotify(CommentMocks.COMMENT_ID_1)
        // Assert
        assertThat(SUT.parentId, `is`(CommentMocks.COMMENT_ID_1))
    }

    @Test
    fun getParentCommentAndNotify_correctCommentIdPassedToGetCachedComment() {
        // Arrange
        // Act
        SUT.getParentCommentAndNotify(CommentMocks.COMMENT_ID_1)
        // Assert
        assertThat(mCommentCacheTd.mGetCachedCommentCounter, `is`(1))
        assertThat(mCommentCacheTd.mGetCachedCommentArgCommentId, `is`(CommentMocks.COMMENT_ID_1))
    }

    @Test
    fun getParentCommentAndNotify_getCachedCommentSuccessNullCommentReturned_listenersNotifiedWithError() {
        // Arrange
        SUT.registerRepliesViewModelListener(mListener1)
        SUT.registerRepliesViewModelListener(mListener2)
        // Act
        SUT.getParentCommentAndNotify(CommentMocks.COMMENT_ID_1)
        // Assert
        verify(mListener1).onParentCommentFetchFailed()
        verify(mListener2).onParentCommentFetchFailed()
    }

    @Test
    fun getParentCommentAndNotify_getCachedCommentSuccessNonNullCommentReturned_listenersNotifiedWithCorrectComment() {
        // Arrange
        getCachedCommentNonNullCommentReturned()
        val ac: ArgumentCaptor<Comment> = ArgumentCaptor.forClass(Comment::class.java)
        SUT.registerRepliesViewModelListener(mListener1)
        SUT.registerRepliesViewModelListener(mListener2)
        // Act
        SUT.getParentCommentAndNotify(CommentMocks.COMMENT_ID_1)
        // Assert
        verify(mListener1).onParentCommentFetched(ac.kotlinCapture())
        verify(mListener2).onParentCommentFetched(ac.kotlinCapture())
        assertThat(ac.allValues[0], `is`(CommentMocks.GET_COMMENT_1()))
        assertThat(ac.allValues[1], `is`(CommentMocks.GET_COMMENT_1()))
    }

    @Test
    fun getParentCommentAndNotify_getCachedCommentError_listenersNotifiedWithError() {
        // Arrange
        getCachedCommentFailure()
        SUT.registerRepliesViewModelListener(mListener1)
        SUT.registerRepliesViewModelListener(mListener2)
        // Act
        SUT.getParentCommentAndNotify(CommentMocks.COMMENT_ID_1)
        // Assert
        verify(mListener1).onParentCommentFetchFailed()
        verify(mListener2).onParentCommentFetchFailed()
    }

    // region helper methods
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