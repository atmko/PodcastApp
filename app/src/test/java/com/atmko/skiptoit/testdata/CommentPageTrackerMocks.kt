package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.CommentPageTracker

class CommentPageTrackerMocks {

    companion object {
        fun COMMENT_PAGE_TRACKER_NULL_NEXT_PAGE(): CommentPageTracker {
            return CommentPageTracker(CommentMocks.COMMENT_ID_1, 1, null, null)
        }

        fun COMMENT_PAGE_TRACKER_NEXT_PAGE(): CommentPageTracker {
            return CommentPageTracker(CommentMocks.COMMENT_ID_1, 1, 2, null)
        }

        fun COMMENT_PAGE_TRACKER_PREV_PAGE(): CommentPageTracker {
            return CommentPageTracker(CommentMocks.COMMENT_ID_1, 2, null, 1)
        }
    }
}