package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, NetworkModule::class, DatabaseModule::class])
interface ApplicationComponent {
    fun inject(app: SkipToItApplication)

    fun newPresentationComponent(presentationModule: PresentationModule): PresentationComponent
}