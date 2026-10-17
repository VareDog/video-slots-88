package com.varedog.videoslots

import android.content.Context

object Prefs {

    private const val FILE = "videoslots"
    private const val KEY_CONTINUOUS = "continuous"

    fun isContinuous(context: Context): Boolean {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_CONTINUOUS, false)
    }

    fun setContinuous(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_CONTINUOUS, value).apply()
    }
}
