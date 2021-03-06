package com.atmko.skiptoit.common.views

import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.AdapterModule
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule

open class BaseActivity : AppCompatActivity() {
    var isInjected: Boolean = false

    @UiThread
    fun getPresentationComponent(): PresentationComponent {
        if (!isInjected) {
            isInjected = true
            return getApplicationComponent()
                .newPresentationComponent(
                    PresentationModule(), AdapterModule(this, this)
                )
        }
        throw RuntimeException("getPresentationComponent() called more than once")
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (application as SkipToItApplication).appComponent
    }
}