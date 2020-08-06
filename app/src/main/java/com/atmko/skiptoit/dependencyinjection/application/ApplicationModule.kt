package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.SkipToItApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val app: SkipToItApplication) {
    @Provides
    @Singleton
    fun provideApplication(): SkipToItApplication {
        return app
    }
}