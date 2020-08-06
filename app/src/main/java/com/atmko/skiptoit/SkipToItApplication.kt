package com.atmko.skiptoit

import android.app.Application
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.application.ApplicationModule
import com.atmko.skiptoit.dependencyinjection.application.DaggerApplicationComponent

class SkipToItApplication : Application() {

    val appComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this);
    }

}