package com.varedog.videoslots

import android.app.AlertDialog
import android.app.Application

class VideoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Api.init(this)
        CrashHandler.install(this)
    }
}
