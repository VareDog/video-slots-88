package com.varedog.videoslots

import android.content.Context
import android.content.Intent
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler private constructor(private val app: Context) : Thread.UncaughtExceptionHandler {

    private val systemHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val text = "时间: " + System.currentTimeMillis() + "\n线程: " + thread.name + "\n\n" + sw.toString()
            File(app.filesDir, "crash.txt").writeText(text)
        } catch (e: Exception) {
        }
        systemHandler?.uncaughtException(thread, throwable)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    companion object {
        fun install(app: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(app))
        }

        fun consumeCrash(context: Context): String? {
            val f = File(context.filesDir, "crash.txt")
            if (!f.exists()) return null
            return try {
                val text = f.readText()
                f.delete()
                text
            } catch (e: Exception) {
                null
            }
        }

        fun showReport(context: Context) {
            context.startActivity(Intent(context, CrashReportActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
