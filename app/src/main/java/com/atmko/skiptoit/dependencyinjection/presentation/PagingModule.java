package com.atmko.skiptoit.dependencyinjection.presentation;

import com.atmko.skiptoit.viewmodel.datasource.GenrePodcastDataSource;
import com.atmko.skiptoit.viewmodel.datasource.PodcastDataSource;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.viewmodel.datasource.QueryPodcastDataSource;
import com.atmko.skiptoit.viewmodel.common.PodcastDataSourceFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.inject.Provider;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class PagingModule {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface PodcastDataSourceKey {
        Class<? extends PodcastDataSource> value();
    }

    @Provides
    PodcastDataSourceFactory providePodcastDataSourceFactory(
            Map<Class<? extends PodcastDataSource>,
            Provider<PodcastDataSource>> providerMap) {
        return new PodcastDataSourceFactory(providerMap);
    }

    @Provides
    @IntoMap
    @PodcastDataSourceKey(GenrePodcastDataSource.class)
    PodcastDataSource provideGenrePodcastDataSource(PodcastsApi podcastApi) {
        return new GenrePodcastDataSource(podcastApi);
    }

    @Provides
    @IntoMap
    @PodcastDataSourceKey(QueryPodcastDataSource.class)
    PodcastDataSource provideQueryPodcastDataSource(PodcastsApi podcastApi) {
        return new QueryPodcastDataSource(podcastApi);
    }
}
