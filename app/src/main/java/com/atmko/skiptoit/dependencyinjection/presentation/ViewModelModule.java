package com.atmko.skiptoit.dependencyinjection.presentation;

import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;

import com.atmko.skiptoit.SkipToItApplication;
import com.atmko.skiptoit.model.PodcastsApi;
import com.atmko.skiptoit.model.SkipToItApi;
import com.atmko.skiptoit.model.database.SubscriptionsDao;
import com.atmko.skiptoit.viewmodel.CommentsViewModel;
import com.atmko.skiptoit.viewmodel.CreateCommentViewModel;
import com.atmko.skiptoit.viewmodel.CreateReplyViewModel;
import com.atmko.skiptoit.viewmodel.DetailsViewModel;
import com.atmko.skiptoit.viewmodel.EpisodeViewModel;
import com.atmko.skiptoit.viewmodel.LaunchFragmentViewModel;
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel;
import com.atmko.skiptoit.viewmodel.RepliesViewModel;
import com.atmko.skiptoit.viewmodel.SearchParentViewModel;
import com.atmko.skiptoit.viewmodel.SearchViewModel;
import com.atmko.skiptoit.viewmodel.SubscriptionsViewModel;
import com.atmko.skiptoit.viewmodel.UpdateCommentViewModel;
import com.atmko.skiptoit.viewmodel.ViewModelFactory;
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
                                      SubscriptionsDao subscriptionsDao) {
        return new DetailsViewModel(
                skipToItApi,
                podcastApi,
                googleSignInClient,
                subscriptionsDao);
    }

    @Provides
    @IntoMap
    @ViewModelKey(EpisodeViewModel.class)
    ViewModel provideEpisodeViewModel(PodcastsApi podcastApi,
                                      @Named("episode_fragment")SharedPreferences sharedPreferences) {
        return new EpisodeViewModel(podcastApi, sharedPreferences);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CommentsViewModel.class)
    ViewModel provideCommentsViewModel(SkipToItApi skipToItApi,
                                       GoogleSignInClient googleSignInClient) {
        return new CommentsViewModel(skipToItApi, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(RepliesViewModel.class)
    ViewModel provideRepliesViewModel(SkipToItApi skipToItApi,
                                      GoogleSignInClient googleSignInClient) {
        return new RepliesViewModel(skipToItApi, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreateCommentViewModel.class)
    ViewModel provideCreateCommentsViewModel(SkipToItApi skipToItApi,
                                             GoogleSignInClient googleSignInClient) {
        return new CreateCommentViewModel(skipToItApi, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(CreateReplyViewModel.class)
    ViewModel provideCreateReplyViewModel(SkipToItApi skipToItApi,
                                             GoogleSignInClient googleSignInClient) {
        return new CreateReplyViewModel(skipToItApi, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(UpdateCommentViewModel.class)
    ViewModel provideUpdateCommentsViewModel(SkipToItApi skipToItApi,
                                       GoogleSignInClient googleSignInClient) {
        return new UpdateCommentViewModel(skipToItApi, googleSignInClient);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchParentViewModel.class)
    ViewModel provideSearchParentViewModel(PodcastsApi podcastApi) {
        return new SearchParentViewModel(podcastApi);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    ViewModel provideSearchViewModel(PodcastsApi podcastApi) {
        return new SearchViewModel(podcastApi);
    }
}
