package com.atmko.skiptoit.dependencyinjection.presentation;

import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.atmko.skiptoit.SkipToItApplication;
import com.atmko.skiptoit.episode.common.CommentsEndpoint;
import com.atmko.skiptoit.createcomment.CommentPageTrackerHelper;
import com.atmko.skiptoit.createcomment.CreateCommentEndpoint;
import com.atmko.skiptoit.createcomment.CreateCommentViewModel;
import com.atmko.skiptoit.createreply.CreateReplyEndpoint;
import com.atmko.skiptoit.createreply.ReplyPageTrackerHelper;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.model.SkipToItApi;
import com.atmko.skiptoit.model.database.CommentCache;
import com.atmko.skiptoit.model.database.CommentDao;
import com.atmko.skiptoit.model.database.CommentPageTrackerDao;
import com.atmko.skiptoit.model.database.SkipToItDatabase;
import com.atmko.skiptoit.model.database.SubscriptionsDao;
import com.atmko.skiptoit.updatecomment.UpdateCommentEndpoint;
import com.atmko.skiptoit.updatecomment.UpdateCommentViewModel;
import com.atmko.skiptoit.createreply.CreateReplyViewModel;
import com.atmko.skiptoit.viewmodel.DetailsViewModel;
import com.atmko.skiptoit.viewmodel.EpisodeViewModel;
import com.atmko.skiptoit.viewmodel.LaunchFragmentViewModel;
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel;
import com.atmko.skiptoit.episode.ParentCommentsViewModel;
import com.atmko.skiptoit.episode.replies.RepliesViewModel;
import com.atmko.skiptoit.viewmodel.SearchParentViewModel;
import com.atmko.skiptoit.viewmodel.SearchViewModel;
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel;
import com.atmko.skiptoit.viewmodel.common.PodcastDataSourceFactory;
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory;
import com.atmko.skiptoit.viewmodel.paging.EpisodeBoundaryCallback;
import com.atmko.skiptoit.episode.ParentCommentBoundaryCallback;
import com.atmko.skiptoit.episode.replies.ReplyCommentBoundaryCallback;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

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
public class ViewModelModule {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface ViewModelKey {
        Class<? extends ViewModel> value();
    }

    //todo consider making singleton
    @Provides
    ViewModelFactory viewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new ViewModelFactory(providerMap);
    }

    @Provides
    @IntoMap
    @ViewModelKey(LaunchFragmentViewModel.class)
    ViewModel provideLaunchFragmentViewModel(@Named("launch_fragment") SharedPreferences sharedPreferences) {
        return new LaunchFragmentViewModel(sharedPreferences);
    }

    @Provides
    @IntoMap
    @ViewModelKey(MasterActivityViewModel.class)
    ViewModel provideMasterActivityViewModel(SkipToItApi skipToItApi,
                                             PodcastsApi podcastApi,
                                             SubscriptionsDao subscriptionsDao,
                                             GoogleSignInClient googleSignInClient,
                                             SkipToItApplication application) {
        return new MasterActivityViewModel(
                skipToItApi,
                podcastApi,
                subscriptionsDao,
                googleSignInClient,
                application);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SubscriptionsViewModel.class)
    ViewModel provideSubscriptionsViewModel(SkipToItApi skipToItApi,
                                            SubscriptionsDao subscriptionsDao,
                                            GoogleSignInClient googleSignInClient) {
        return new SubscriptionsViewModel(skipToItApi, subscriptionsDao, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(DetailsViewModel.class)
    ViewModel provideDetailsViewModel(SkipToItApi skipToItApi,
                                      PodcastsApi podcastApi,
                                      GoogleSignInClient googleSignInClient,
                                      SkipToItDatabase skipToItDatabase,
                                      EpisodeBoundaryCallback episodeBoundaryCallback,
                                      @Named("episodes") PagedList.Config pagedListConfig) {
        return new DetailsViewModel(
                skipToItApi,
                podcastApi,
                googleSignInClient,
                skipToItDatabase,
                episodeBoundaryCallback,
                pagedListConfig);
    }

    @Provides
    @IntoMap
    @ViewModelKey(EpisodeViewModel.class)
    ViewModel provideEpisodeViewModel(PodcastsApi podcastApi,
                                      SkipToItDatabase skipToItDatabase,
                                      @Named("episode_fragment") SharedPreferences sharedPreferences) {
        return new EpisodeViewModel(podcastApi, skipToItDatabase, sharedPreferences);
    }

    @Provides
    @IntoMap
    @ViewModelKey(ParentCommentsViewModel.class)
    ViewModel provideParentCommentsViewModel(CommentsEndpoint commentsEndpoint,
                                             CommentCache commentCache,
                                             CommentDao commentDao,
                                             ParentCommentBoundaryCallback parentCommentMediator,
                                             @Named("comments") PagedList.Config pagedListConfig) {
        return new ParentCommentsViewModel(
                commentsEndpoint, commentCache, commentDao, parentCommentMediator, pagedListConfig);
    }

    @Provides
    @IntoMap
    @ViewModelKey(RepliesViewModel.class)
    ViewModel provideRepliesViewModel(CommentsEndpoint commentsEndpoint,
                                      CommentCache commentCache,
                                      CommentDao commentDao,
                                      ReplyCommentBoundaryCallback replyCommentMediator,
                                      @Named("comments") PagedList.Config pagedListConfig) {
        return new RepliesViewModel(
                commentsEndpoint, commentCache, commentDao, replyCommentMediator, pagedListConfig);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreateCommentViewModel.class)
    ViewModel provideCreateCommentsViewModel(CreateCommentEndpoint createCommentEndpoint,
                                             CommentPageTrackerHelper commentPageTrackerHelper) {
        return new CreateCommentViewModel(createCommentEndpoint, commentPageTrackerHelper);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreateReplyViewModel.class)
    ViewModel provideCreateReplyViewModel(CreateReplyEndpoint createReplyEndpoint,
                                          ReplyPageTrackerHelper replyPageTrackerHelper) {
        return new CreateReplyViewModel(createReplyEndpoint, replyPageTrackerHelper);
    }

    @Provides
    @IntoMap
    @ViewModelKey(UpdateCommentViewModel.class)
    ViewModel provideUpdateCommentsViewModel(UpdateCommentEndpoint updateCommentEndpoint,
                                             CommentCache commentCache) {
        return new UpdateCommentViewModel(updateCommentEndpoint, commentCache);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchParentViewModel.class)
    ViewModel provideSearchParentViewModel(PodcastDataSourceFactory podcastDataSourceFactory) {
        return new SearchParentViewModel(podcastDataSourceFactory);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    ViewModel provideSearchViewModel(PodcastDataSourceFactory podcastDataSourceFactory) {
        return new SearchViewModel(podcastDataSourceFactory);
    }

    //------------
    @Provides
    CommentCache provideCommentCache(CommentDao commentDao) {
        return new CommentCache(commentDao);
    }

    @Provides
    UpdateCommentEndpoint provideUpdateCommentEndpoint(SkipToItApi skipToItApi,
                                                       GoogleSignInClient googleSignInClient) {
        return new UpdateCommentEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    CreateCommentEndpoint provideCreateCommentEndpoint(SkipToItApi skipToItApi,
                                                       GoogleSignInClient googleSignInClient) {
        return new CreateCommentEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    CommentPageTrackerHelper provideCommentCommentPageTrackerHelper(
            CommentDao commentDao,
            CommentPageTrackerDao commentPageTrackerDao) {
        return new CommentPageTrackerHelper(commentDao, commentPageTrackerDao);
    }

    @Provides
    CreateReplyEndpoint provideCreateReplyEndpoint(SkipToItApi skipToItApi,
                                                       GoogleSignInClient googleSignInClient) {
        return new CreateReplyEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    ReplyPageTrackerHelper provideReplyCommentPageTrackerHelper(
            CommentDao commentDao,
            CommentPageTrackerDao commentPageTrackerDao) {
        return new ReplyPageTrackerHelper(commentDao, commentPageTrackerDao);
    }

    @Provides
    CommentsEndpoint provideCommentsEndpoint(SkipToItApi skipToItApi,
                                                GoogleSignInClient googleSignInClient) {
        return new CommentsEndpoint(skipToItApi, googleSignInClient);
    }
}
