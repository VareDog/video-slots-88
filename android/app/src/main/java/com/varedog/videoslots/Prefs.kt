package com.varedog.videoslots

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object Prefs {

    private const val FILE = "videoslots"
    private const val KEY_CONTINUOUS = "continuous"
    private const val KEY_LIST_CACHE = "list_cache"

    fun isContinuous(context: Context): Boolean {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_CONTINUOUS, false)
    }

    fun setContinuous(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_CONTINUOUS, value).apply()
    }

    fun saveListCache(context: Context, items: List<Api.VideoItem>) {
        try {
            val arr = JSONArray()
            for (v in items) {
                val o = JSONObject()
                o.put("slot", v.slot)
                o.put("title", v.title)
                o.put("videoUrl", v.videoUrl)
                o.put("coverUrl", v.coverUrl ?: JSONObject.NULL)
                o.put("size", v.size)
                arr.put(o)
            }
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY_LIST_CACHE, arr.toString()).apply()
        } catch (e: Exception) {
        }
    }

    fun loadListCache(context: Context): List<Api.VideoItem>? {
        return try {
            val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_LIST_CACHE, null)
                ?: return null
            val arr = JSONArray(raw)
            val out = ArrayList<Api.VideoItem>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    Api.VideoItem(
                        slot = o.optInt("slot", 0),
                        title = o.optString("title", ""),
                        videoUrl = o.optString("videoUrl", ""),
                        coverUrl = if (o.isNull("coverUrl")) null else o.optString("coverUrl"),
                        size = o.optLong("size", 0)
                    )
                )
            }
            out
        } catch (e: Exception) {
            null
        }
    }
}
