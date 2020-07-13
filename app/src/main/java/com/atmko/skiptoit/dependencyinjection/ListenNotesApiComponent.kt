package com.atmko.skiptoit.dependencyinjection

import com.atmko.skiptoit.model.PodcastsService
import com.atmko.skiptoit.viewmodel.DetailsViewModel
import com.atmko.skiptoit.viewmodel.EpisodeViewModel
import com.atmko.skiptoit.viewmodel.SearchViewModel
import dagger.Component

@Component(modules = [ListenNotesApiModule::class])
interface ListenNotesApiComponent {
    fun inject(service: PodcastsService)
    fun inject(service: SearchViewModel)
    fun inject(service: DetailsViewModel)
    fun inject(service: EpisodeViewModel)
}