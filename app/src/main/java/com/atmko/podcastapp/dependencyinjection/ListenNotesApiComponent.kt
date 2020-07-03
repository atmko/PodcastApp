package com.atmko.podcastapp.dependencyinjection

import com.atmko.podcastapp.model.PodcastsService
import com.atmko.podcastapp.viewmodel.DetailsViewModel
import com.atmko.podcastapp.viewmodel.EpisodeViewModel
import com.atmko.podcastapp.viewmodel.SearchViewModel
import dagger.Component

@Component(modules = [ListenNotesApiModule::class])
interface ListenNotesApiComponent {
    fun inject(service: PodcastsService)
    fun inject(service: SearchViewModel)
    fun inject(service: DetailsViewModel)
    fun inject(service: EpisodeViewModel)
}