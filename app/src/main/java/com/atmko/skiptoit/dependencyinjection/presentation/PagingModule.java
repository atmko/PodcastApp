package com.atmko.skiptoit.dependencyinjection.presentation;

import androidx.paging.PagedList;

import com.atmko.skiptoit.viewmodel.paging.ParentCommentBoundaryCallback;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.viewmodel.paging.ReplyCommentBoundaryCallback;
import com.atmko.skiptoit.model.SkipToItApi;
import com.atmko.skiptoit.model.database.SkipToItDatabase;
import com.atmko.skiptoit.viewmodel.common.PodcastDataSourceFactory;
import com.atmko.skiptoit.viewmodel.datasource.GenrePodcastDataSource;
import com.atmko.skiptoit.viewmodel.datasource.PodcastDataSource;
import com.atmko.skiptoit.viewmodel.datasource.QueryPodcastDataSource;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

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

import static com.atmko.skiptoit.viewmodel.common.CommentsViewModelKt.enablePlaceholders;
import static com.atmko.skiptoit.viewmodel.common.CommentsViewModelKt.initialLoadSize;
import static com.atmko.skiptoit.viewmodel.common.CommentsViewModelKt.maxSize;
import static com.atmko.skiptoit.viewmodel.common.CommentsViewModelKt.pageSize;
import static com.atmko.skiptoit.viewmodel.common.CommentsViewModelKt.prefetchDistance;

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

    @Provides
    ParentCommentBoundaryCallback provideParentCommentMediator(GoogleSignInClient googleSignInClient,
                                                       SkipToItApi skipToItApi,
                                                       SkipToItDatabase skipToItDatabase) {
        return new ParentCommentBoundaryCallback(googleSignInClient, skipToItApi, skipToItDatabase);
    }

    @Provides
    ReplyCommentBoundaryCallback provideReplyCommentMediator(GoogleSignInClient googleSignInClient,
                                                     SkipToItApi skipToItApi,
                                                     SkipToItDatabase skipToItDatabase) {
        return new ReplyCommentBoundaryCallback(googleSignInClient, skipToItApi, skipToItDatabase);
    }

    @Provides
    PagedList.Config providePagingListConfig() {
        return new PagedList.Config.Builder()
                .setPageSize(pageSize)
                .setPrefetchDistance(prefetchDistance)
                .setEnablePlaceholders(enablePlaceholders)
                .setInitialLoadSizeHint(initialLoadSize)
                .setMaxSize(maxSize)
                .build();
    }
}
