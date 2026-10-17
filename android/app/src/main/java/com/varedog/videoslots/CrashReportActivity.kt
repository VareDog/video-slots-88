package com.varedog.videoslots

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class CrashReportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val log = findViewById<TextView>(R.id.crashLog)
        log.text = CrashHandler.consumeCrash(this) ?: "未找到崩溃日志"
        log.setTextIsSelectable(true)

        findViewById<Button>(R.id.continueBtn).setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
