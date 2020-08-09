package com.atmko.skiptoit.dependencyinjection.service

import com.atmko.skiptoit.services.PlaybackService
import dagger.Subcomponent

@Subcomponent(modules = [ServiceModule::class])
interface ServiceComponent {
    fun inject(playbackService: PlaybackService)
}