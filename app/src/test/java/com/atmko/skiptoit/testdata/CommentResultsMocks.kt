package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.CommentResults

class CommentResultsMocks {

    companion object {

        fun GET_COMMENT_RESULTS(): CommentResults {
            return CommentResults(
                listOf(
                    CommentMocks.GET_COMMENT_1(),
                    CommentMocks.GET_COMMENT_2()
                ), 1, false, false
            )
        }
    }

}