package com.atmko.podcastapp.dependencyinjection

import com.atmko.podcastapp.model.SkipToItService
import com.atmko.podcastapp.viewmodel.CommentsViewModel
import dagger.Component

@Component(modules = [SkipToItApiModule::class])
interface SkipToItApiComponent {
    fun inject(service: SkipToItService)
    fun inject(service: CommentsViewModel)
}