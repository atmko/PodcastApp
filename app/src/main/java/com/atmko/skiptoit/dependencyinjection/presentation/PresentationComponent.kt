package com.atmko.skiptoit.dependencyinjection.presentation

import com.atmko.skiptoit.view.MasterActivity
import dagger.Subcomponent

@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
    fun inject(masterActivity: MasterActivity)
}