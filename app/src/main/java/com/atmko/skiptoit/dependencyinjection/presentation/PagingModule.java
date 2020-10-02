package com.atmko.skiptoit.dependencyinjection.presentation;

import android.content.SharedPreferences;

import androidx.paging.PagedList;

import com.atmko.skiptoit.episode.GetCommentsEndpoint;
import com.atmko.skiptoit.episode.ParentCommentBoundaryCallback;
import com.atmko.skiptoit.episode.common.CommentsViewModel;
import com.atmko.skiptoit.episode.replies.GetRepliesEndpoint;
import com.atmko.skiptoit.episode.replies.ReplyCommentBoundaryCallback;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.model.database.CommentCache;
import com.atmko.skiptoit.model.database.SkipToItDatabase;
import com.atmko.skiptoit.details.DetailsViewModel;
import com.atmko.skiptoit.viewmodel.common.PodcastDataSourceFactory;
import com.atmko.skiptoit.viewmodel.datasource.GenrePodcastDataSource;
import com.atmko.skiptoit.viewmodel.datasource.PodcastDataSource;
import com.atmko.skiptoit.viewmodel.datasource.QueryPodcastDataSource;
import com.atmko.skiptoit.viewmodel.paging.EpisodeBoundaryCallback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.inject.Named;
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

    @Provides
    EpisodeBoundaryCallback provideReplyEpisodeBoundaryCallback(PodcastsApi podcastsApi,
                                                                SkipToItDatabase skipToItDatabase,
                                                                @Named("episode_fragment") SharedPreferences sharedPreferences) {
        return new EpisodeBoundaryCallback(podcastsApi, skipToItDatabase, sharedPreferences);
    }

    @Provides
    ParentCommentBoundaryCallback provideParentCommentBoundaryCallback(GetCommentsEndpoint getCommentsEndpoint,
                                                                       CommentCache commentCache) {
        return new ParentCommentBoundaryCallback(getCommentsEndpoint, commentCache);
    }

    @Provides
    ReplyCommentBoundaryCallback provideReplyCommentBoundaryCallback(GetRepliesEndpoint getRepliesEndpoint,
                                                                     CommentCache commentCache) {
        return new ReplyCommentBoundaryCallback(getRepliesEndpoint, commentCache);
    }

    @Provides
    @Named("episodes")
    PagedList.Config provideEpisodePagingListConfig() {
        return new PagedList.Config.Builder()
                .setPageSize(DetailsViewModel.pageSize)
                .setPrefetchDistance(DetailsViewModel.prefetchDistance)
                .setEnablePlaceholders(DetailsViewModel.enablePlaceholders)
                .setInitialLoadSizeHint(DetailsViewModel.initialLoadSize)
                .setMaxSize(DetailsViewModel.maxSize)
                .build();
    }

    @Provides
    @Named("comments")
    PagedList.Config provideCommentPagingListConfig() {
        return new PagedList.Config.Builder()
                .setPageSize(CommentsViewModel.pageSize)
                .setPrefetchDistance(CommentsViewModel.prefetchDistance)
                .setEnablePlaceholders(CommentsViewModel.enablePlaceholders)
                .setInitialLoadSizeHint(CommentsViewModel.initialLoadSize)
                .setMaxSize(CommentsViewModel.maxSize)
                .build();
    }
}
