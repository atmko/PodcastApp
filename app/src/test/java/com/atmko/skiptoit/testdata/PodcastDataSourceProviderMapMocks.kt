package com.atmko.skiptoit.testdata

import com.atmko.skiptoit.search.common.PodcastDataSource
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource
import com.atmko.skiptoit.search.searchparent.GenrePodcastDataSource
import com.atmko.skiptoit.testclass.GenrePodcastDataSourceProviderTd
import com.atmko.skiptoit.testclass.QueryPodcastDataSourceProviderTd
import javax.inject.Provider

class PodcastDataSourceProviderMapMocks {

    companion object {
        fun GET_PROVIDER_MAP():Map<Class<out PodcastDataSource>, Provider<PodcastDataSource>>  {
            val providerMap: MutableMap<Class<out PodcastDataSource>, Provider<PodcastDataSource>> = mutableMapOf()
            providerMap[QueryPodcastDataSource::class.java] = QueryPodcastDataSourceProviderTd()
            providerMap[GenrePodcastDataSource::class.java] = GenrePodcastDataSourceProviderTd()
            return providerMap
        }
    }
}