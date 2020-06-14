package com.atmko.podcastapp.dependencyinjection

import com.atmko.podcastapp.model.PodcastsService
import com.atmko.podcastapp.viewmodel.SearchViewModel
import dagger.Component

@Component(modules = [ApiModule::class])
interface ApiComponent {
    fun inject(service: PodcastsService)
    fun inject(service: SearchViewModel)
}