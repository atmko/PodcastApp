package com.atmko.skiptoit.dependencyinjection

import com.atmko.skiptoit.model.SkipToItService
import com.atmko.skiptoit.viewmodel.CommentsViewModel
import dagger.Component

@Component(modules = [SkipToItApiModule::class])
interface SkipToItApiComponent {
    fun inject(service: SkipToItService)
    fun inject(service: CommentsViewModel)
}