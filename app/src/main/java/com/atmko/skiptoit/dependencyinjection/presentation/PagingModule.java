package com.atmko.skiptoit.dependencyinjection.presentation;

import androidx.paging.PagedList;

import com.atmko.skiptoit.episode.GetCommentsEndpoint;
import com.atmko.skiptoit.episode.ParentCommentBoundaryCallback;
import com.atmko.skiptoit.episode.common.CommentsViewModel;
import com.atmko.skiptoit.episode.replies.GetRepliesEndpoint;
import com.atmko.skiptoit.episode.replies.ReplyCommentBoundaryCallback;
import com.atmko.skiptoit.episodelist.EpisodeBoundaryCallback;
import com.atmko.skiptoit.episodelist.EpisodeListViewModel;
import com.atmko.skiptoit.episodelist.GetEpisodesEndpoint;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.model.database.CommentCache;
import com.atmko.skiptoit.model.database.EpisodesCache;
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory;
import com.atmko.skiptoit.search.searchparent.GenrePodcastDataSource;
import com.atmko.skiptoit.search.common.PodcastDataSource;
import com.atmko.skiptoit.search.searchchild.QueryPodcastDataSource;
import com.atmko.skiptoit.utils.AppExecutors;

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
    PodcastDataSource provideGenrePodcastDataSource(PodcastsApi podcastApi, AppExecutors appExecutors) {
        return new GenrePodcastDataSource(podcastApi, appExecutors);
    }

    @Provides
    @IntoMap
    @PodcastDataSourceKey(QueryPodcastDataSource.class)
    PodcastDataSource provideQueryPodcastDataSource(PodcastsApi podcastApi, AppExecutors appExecutors) {
        return new QueryPodcastDataSource(podcastApi, appExecutors);
    }

    @Provides
    EpisodeBoundaryCallback provideReplyEpisodeBoundaryCallback(GetEpisodesEndpoint getEpisodesEndpoint,
                                                                EpisodesCache episodesCache) {
        return new EpisodeBoundaryCallback(getEpisodesEndpoint, episodesCache);
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
                .setPageSize(EpisodeListViewModel.pageSize)
                .setPrefetchDistance(EpisodeListViewModel.prefetchDistance)
                .setEnablePlaceholders(EpisodeListViewModel.enablePlaceholders)
                .setInitialLoadSizeHint(EpisodeListViewModel.initialLoadSize)
                .setMaxSize(EpisodeListViewModel.maxSize)
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

    @Provides
    @Named("episode_list")
    PagedList.Config provideEpisodeListPagingListConfig() {
        return new PagedList.Config.Builder()
                .setPageSize(EpisodeListViewModel.pageSize)
                .setPrefetchDistance(EpisodeListViewModel.prefetchDistance)
                .setEnablePlaceholders(EpisodeListViewModel.enablePlaceholders)
                .setInitialLoadSizeHint(EpisodeListViewModel.initialLoadSize)
                .setMaxSize(EpisodeListViewModel.maxSize)
                .build();
    }
}
