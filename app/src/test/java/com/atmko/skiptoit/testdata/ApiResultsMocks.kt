package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.model.ApiResults

class ApiResultsMocks {

    companion object {

        fun GET_API_RESULTS(): ApiResults {
            return ApiResults(
                listOf(
                    PodcastMocks.GET_PODCAST_1(),
                    PodcastMocks.GET_PODCAST_2()
                ), true
            )
        }
    }

}