package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.presentation.AdapterModule
import com.atmko.skiptoit.dependencyinjection.presentation.PagingModule
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule
import com.atmko.skiptoit.dependencyinjection.service.ServiceComponent
import com.atmko.skiptoit.dependencyinjection.service.ServiceModule
import com.atmko.skiptoit.services.BecomingNoisyReceiver
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ApplicationModule::class,
    NetworkModule::class,
    DatabaseModule::class,
    ExoPlayerModule::class])
interface ApplicationComponent {
    fun inject(app: SkipToItApplication)
    fun inject(app: BecomingNoisyReceiver)

    fun newPresentationComponent(
        presentationModule: PresentationModule,
        adapterModule: AdapterModule
    ): PresentationComponent

    fun newServiceComponent(serviceModule: ServiceModule): ServiceComponent
}