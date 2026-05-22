package com.example.musicdownloader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicDownloaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
