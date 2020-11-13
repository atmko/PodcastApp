package com.atmko.skiptoit.services.common

import androidx.annotation.UiThread
import androidx.core.app.JobIntentService
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.service.ServiceComponent
import com.atmko.skiptoit.dependencyinjection.service.ServiceModule

abstract class BaseService : JobIntentService() {
    private var mIsServiceComponentUsed = false

    @UiThread
    protected fun getServiceComponent() : ServiceComponent {
        if (!mIsServiceComponentUsed) {
            mIsServiceComponentUsed = true
            return getApplicationComponent()
                .newServiceComponent(
                    ServiceModule(
                        this
                    )
                )
        }
        throw RuntimeException("getPresentationComponent() called more than once")
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (application as SkipToItApplication).appComponent
    }
}