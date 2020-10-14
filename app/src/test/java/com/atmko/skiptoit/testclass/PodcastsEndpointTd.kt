package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.PodcastsEndpoint
import com.atmko.skiptoit.testdata.ApiResultsMocks

class PodcastsEndpointTd : PodcastsEndpoint(null, null) {

    var mGetBatchPodcastMetadataCounter = 0
    var mGetBatchPodcastMetadataFailure = false
    lateinit var mCombinedPodcastIds: String
    override fun getBatchPodcastMetadata(
        combinedPodcastIds: String,
        listener: BatchFetchPodcastsListener
    ) {
        mGetBatchPodcastMetadataCounter += 1
        mCombinedPodcastIds = combinedPodcastIds
        if (!mGetBatchPodcastMetadataFailure) {
            listener.onBatchFetchSuccess(ApiResultsMocks.GET_API_RESULTS())
        } else {
            listener.onBatchFetchFailed()
        }
    }
}