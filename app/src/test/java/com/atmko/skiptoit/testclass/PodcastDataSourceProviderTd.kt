package com.atmko.skiptoit.testclass

import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource
import com.atmko.skiptoit.search.searchparent.GenrePodcastDataSource
import javax.inject.Provider

class QueryPodcastDataSourceProviderTd : Provider<PodcastDataSource> {
    override fun get(): QueryPodcastDataSource {
        return QueryPodcastDataSource(null, null)
    }
}

class GenrePodcastDataSourceProviderTd : Provider<PodcastDataSource> {
    override fun get(): GenrePodcastDataSource {
        return GenrePodcastDataSource(null, null)
    }
}
