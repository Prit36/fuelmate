package com.example.fuelmate

import android.app.Application
import com.example.fuelmate.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FuelMateApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@FuelMateApplication)
            androidLogger(Level.ERROR)
            modules(appModule)
        }
    }
}