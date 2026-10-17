package com.varedog.videoslots

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val crash = CrashHandler.consumeCrash(this)
        if (crash != null) {
            CrashHandler.showReport(this)
            finish()
            return
        }

        try {
            layoutInflater.inflate(R.layout.activity_main, findViewById(android.R.id.content), false)
            val parsed = Api.BASE_URL + "/api/videos"
            if (parsed.isEmpty()) throw IllegalStateException("bad api url")
        } catch (e: Exception) {
            CrashHandler.showReport(this)
            finish()
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
