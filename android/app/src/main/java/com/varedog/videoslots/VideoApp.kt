package com.varedog.videoslots

import android.app.Application

class VideoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
    }
}
