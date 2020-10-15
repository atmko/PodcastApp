package com.atmko.skiptoit.dependencyinjection.presentation;

import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.atmko.skiptoit.LoginManager;
import com.atmko.skiptoit.MasterActivityViewModel;
import com.atmko.skiptoit.PodcastsEndpoint;
import com.atmko.skiptoit.UserEndpoint;
import com.atmko.skiptoit.search.common.PodcastDataSourceFactory;
import com.atmko.skiptoit.common.ViewModelFactory;
import com.atmko.skiptoit.confirmation.ConfirmationViewModel;
import com.atmko.skiptoit.createcomment.CreateCommentEndpoint;
import com.atmko.skiptoit.createcomment.CreateCommentViewModel;
import com.atmko.skiptoit.createreply.CreateReplyEndpoint;
import com.atmko.skiptoit.createreply.CreateReplyViewModel;
import com.atmko.skiptoit.details.DetailsViewModel;
import com.atmko.skiptoit.details.PodcastDetailsEndpoint;
import com.atmko.skiptoit.episode.EpisodeEndpoint;
import com.atmko.skiptoit.episode.EpisodeViewModel;
import com.atmko.skiptoit.episode.GetCommentsEndpoint;
import com.atmko.skiptoit.episode.ParentCommentBoundaryCallback;
import com.atmko.skiptoit.episode.ParentCommentsViewModel;
import com.atmko.skiptoit.episode.common.CommentsEndpoint;
import com.atmko.skiptoit.episode.replies.GetRepliesEndpoint;
import com.atmko.skiptoit.episode.replies.RepliesViewModel;
import com.atmko.skiptoit.episode.replies.ReplyCommentBoundaryCallback;
import com.atmko.skiptoit.episodelist.EpisodeBoundaryCallback;
import com.atmko.skiptoit.episodelist.EpisodeListViewModel;
import com.atmko.skiptoit.episodelist.GetEpisodesEndpoint;
import com.atmko.skiptoit.launch.LaunchViewModel;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.model.SkipToItApi;
import com.atmko.skiptoit.model.database.CommentCache;
import com.atmko.skiptoit.model.database.CommentDao;
import com.atmko.skiptoit.model.database.EpisodeDao;
import com.atmko.skiptoit.model.database.EpisodesCache;
import com.atmko.skiptoit.model.database.SkipToItDatabase;
import com.atmko.skiptoit.model.database.SubscriptionsCache;
import com.atmko.skiptoit.model.database.SubscriptionsDao;
import com.atmko.skiptoit.search.searchchild.SearchViewModel;
import com.atmko.skiptoit.search.searchparent.SearchParentViewModel;
import com.atmko.skiptoit.subcriptions.SubscriptionsEndpoint;
import com.atmko.skiptoit.subcriptions.SubscriptionsViewModel;
import com.atmko.skiptoit.updatecomment.UpdateCommentEndpoint;
import com.atmko.skiptoit.updatecomment.UpdateCommentViewModel;
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
    @ViewModelKey(LaunchViewModel.class)
    ViewModel provideLaunchFragmentViewModel(LoginManager loginManager,
                                             UserEndpoint userEndpoint,
                                             EpisodesCache episodesCache) {
        return new LaunchViewModel(loginManager, userEndpoint, episodesCache);
    }

    @Provides
    @IntoMap
    @ViewModelKey(MasterActivityViewModel.class)
    ViewModel provideMasterActivityViewModel(LoginManager loginManager,
                                             UserEndpoint userEndpoint,
                                             EpisodesCache episodesCache) {
        return new MasterActivityViewModel(loginManager, userEndpoint, episodesCache);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SubscriptionsViewModel.class)
    ViewModel provideSubscriptionsViewModel(LoginManager loginManager,
                                            PodcastsEndpoint podcastsEndpoint,
                                            SubscriptionsEndpoint subscriptionsEndpoint,
                                            SubscriptionsCache subscriptionsCache,
                                            SubscriptionsDao subscriptionsDao) {
        return new SubscriptionsViewModel(loginManager, podcastsEndpoint, subscriptionsEndpoint, subscriptionsCache, subscriptionsDao);
    }

    @Provides
    @IntoMap
    @ViewModelKey(DetailsViewModel.class)
    ViewModel provideDetailsViewModel(PodcastDetailsEndpoint podcastDetailsEndpoint,
                                      SubscriptionsEndpoint subscriptionsEndpoint,
                                      SubscriptionsCache subscriptionsCache) {
        return new DetailsViewModel(
                podcastDetailsEndpoint,
                subscriptionsEndpoint,
                subscriptionsCache);
    }

    @Provides
    @IntoMap
    @ViewModelKey(EpisodeListViewModel.class)
    ViewModel provideEpisodeListViewModel(EpisodeDao episodeDao,
                                          EpisodeBoundaryCallback episodeBoundaryCallback,
                                          @Named("episode_list") PagedList.Config pagedListConfig) {
        return new EpisodeListViewModel(
                episodeDao,
                episodeBoundaryCallback,
                pagedListConfig);
    }

    @Provides
    @IntoMap
    @ViewModelKey(EpisodeViewModel.class)
    ViewModel provideEpisodeViewModel(EpisodeEndpoint episodeEndpoint,
                                      EpisodesCache episodesCache) {
        return new EpisodeViewModel(episodeEndpoint, episodesCache);
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
                                             CommentCache commentCache) {
        return new CreateCommentViewModel(createCommentEndpoint, commentCache);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreateReplyViewModel.class)
    ViewModel provideCreateReplyViewModel(CreateReplyEndpoint createReplyEndpoint,
                                          CommentCache commentCache) {
        return new CreateReplyViewModel(createReplyEndpoint, commentCache);
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

    @Provides
    @IntoMap
    @ViewModelKey(ConfirmationViewModel.class)
    ViewModel provideConfirmationViewModel(UserEndpoint userEndpoint) {
        return new ConfirmationViewModel(userEndpoint);
    }

    //------------
    @Provides
    CommentCache provideCommentCache(SkipToItDatabase skipToItDatabase) {
        return new CommentCache(skipToItDatabase);
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
    CreateReplyEndpoint provideCreateReplyEndpoint(SkipToItApi skipToItApi,
                                                   GoogleSignInClient googleSignInClient) {
        return new CreateReplyEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    CommentsEndpoint provideCommentsEndpoint(SkipToItApi skipToItApi,
                                             GoogleSignInClient googleSignInClient) {
        return new CommentsEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    GetCommentsEndpoint provideGetCommentsEndpoint(SkipToItApi skipToItApi,
                                                   GoogleSignInClient googleSignInClient) {
        return new GetCommentsEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    GetRepliesEndpoint provideGetRepliesEndpoint(SkipToItApi skipToItApi,
                                                 GoogleSignInClient googleSignInClient) {
        return new GetRepliesEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    SubscriptionsEndpoint provideSubscriptionsEndpoint(SkipToItApi skipToItApi,
                                                       GoogleSignInClient googleSignInClient) {
        return new SubscriptionsEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    SubscriptionsCache provideSubscriptionsCache(SubscriptionsDao subscriptionsDao,
                                                 @Named("subscriptions") SharedPreferences sharedPreferences) {
        return new SubscriptionsCache(subscriptionsDao);
    }

    @Provides
    PodcastDetailsEndpoint providePodcastDetailsEndpoint(PodcastsApi podcastsApi) {
        return new PodcastDetailsEndpoint(podcastsApi);
    }

    @Provides
    GetEpisodesEndpoint providePodcastApi(PodcastsApi podcastsApi) {
        return new GetEpisodesEndpoint(podcastsApi);
    }

    @Provides
    EpisodeEndpoint provideEpisodeEndpoint(PodcastsApi podcastsApi) {
        return new EpisodeEndpoint(podcastsApi);
    }

    @Provides
    EpisodesCache provideEpisodesCache(SkipToItDatabase skipToItDatabase,
                                       @Named("episode_fragment") SharedPreferences sharedPreferences) {
        return new EpisodesCache(skipToItDatabase, sharedPreferences);
    }

    @Provides
    LoginManager provideLoginManager(GoogleSignInClient googleSignInClient,
                                     @Named("login_manager") SharedPreferences sharedPreferences,
                                     SkipToItDatabase skipToItDatabase) {
        return new LoginManager(googleSignInClient, sharedPreferences, skipToItDatabase);
    }

    @Provides
    UserEndpoint provideUserEndpoint(SkipToItApi skipToItApi,
                                     GoogleSignInClient googleSignInClient) {
        return new UserEndpoint(skipToItApi, googleSignInClient);
    }

    @Provides
    PodcastsEndpoint providePodcastsEndpoint(PodcastsApi podcastsApi,
                                             GoogleSignInClient googleSignInClient) {
        return new PodcastsEndpoint(podcastsApi, googleSignInClient);
    }
}
