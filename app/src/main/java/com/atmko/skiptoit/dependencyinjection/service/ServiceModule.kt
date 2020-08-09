package com.atmko.skiptoit.dependencyinjection.service

import android.app.Service
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ServiceModule(val service: Service) {

    @Provides
    fun service(): Service {
        return service
    }

    @Provides
    fun context(service: Service): Context {
        return service
    }
}