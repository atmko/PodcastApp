package com.atmko.skiptoit.dependencyinjection.presentation

import android.content.Context
import android.content.SharedPreferences
import com.atmko.skiptoit.LoginManager.Companion.LOGIN_MANAGER_KEY
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.episode.EPISODE_FRAGMENT_KEY
import com.atmko.skiptoit.model.database.SubscriptionsCache
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class PresentationModule() {

    @Provides
    @Named("episode_fragment")
    fun provideEpisodeFragmentSharedPreferences(
        skipToItApplication: SkipToItApplication
    ): SharedPreferences {
        return skipToItApplication.getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)
    }

    @Provides
    @Named("login_manager")
    fun provideLaunchFragmentSharedPreferences(
        skipToItApplication: SkipToItApplication
    ): SharedPreferences {
        return skipToItApplication.getSharedPreferences(LOGIN_MANAGER_KEY, Context.MODE_PRIVATE)
    }

    @Provides
    @Named("subscriptions")
    fun provideSubscriptionsCacheSharedPreferences(
        skipToItApplication: SkipToItApplication
    ): SharedPreferences {
        return skipToItApplication.getSharedPreferences(
            SubscriptionsCache.SUBSCRIPTIONS_CACHE_KEY,
            Context.MODE_PRIVATE
        )
    }
}