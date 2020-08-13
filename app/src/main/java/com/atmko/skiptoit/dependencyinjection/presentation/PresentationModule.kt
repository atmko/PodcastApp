package com.atmko.skiptoit.dependencyinjection.presentation

import android.content.Context
import android.content.SharedPreferences
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.view.EPISODE_FRAGMENT_KEY
import com.atmko.skiptoit.view.LAUNCH_FRAGMENT_KEY
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
    @Named("launch_fragment")
    fun provideLaunchFragmentSharedPreferences(
        skipToItApplication: SkipToItApplication
    ): SharedPreferences {
        return skipToItApplication.getSharedPreferences(LAUNCH_FRAGMENT_KEY, Context.MODE_PRIVATE)
    }
}