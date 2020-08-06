package com.atmko.skiptoit.dependencyinjection.application

import com.atmko.skiptoit.viewmodel.*
import dagger.Component

@Component(modules = [ListenNotesApiModule::class])
interface ListenNotesApiComponent {
    //listen notes
    fun inject(service: SearchViewModel)
    fun inject(service: DetailsViewModel)
    fun inject(service: EpisodeViewModel)

    //skip to it
    fun inject(service: CommentsViewModel)
    fun inject(service: SubscriptionsViewModel)
    fun inject(service: MasterActivityViewModel)
}