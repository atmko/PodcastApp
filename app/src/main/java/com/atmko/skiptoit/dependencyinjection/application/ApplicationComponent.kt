package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule
import com.atmko.skiptoit.viewmodel.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ListenNotesApiModule::class])
interface ApplicationComponent {
    fun inject(app: SkipToItApplication)

    fun newPresentationComponent(presentationModule: PresentationModule): PresentationComponent

    //listen notes
    fun inject(service: SearchViewModel)
    fun inject(service: DetailsViewModel)
    fun inject(service: EpisodeViewModel)

    //skip to it
    fun inject(service: CommentsViewModel)
    fun inject(service: SubscriptionsViewModel)
    fun inject(service: MasterActivityViewModel)
}