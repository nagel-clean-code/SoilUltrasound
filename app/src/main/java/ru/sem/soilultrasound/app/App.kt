package ru.sem.soilultrasound.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {

        private lateinit var instance: App

        fun get(): App {
            return instance
        }
    }
}