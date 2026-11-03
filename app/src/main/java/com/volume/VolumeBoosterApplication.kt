package com.volume

import android.app.Application
import com.volume.di.AppContainer
import com.volume.di.DefaultAppContainer

class VolumeBoosterApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
