package com.varedog.videoslots

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object Api {

    const val BASE_URL = "https://tv.988199.xyz"
    const val HOST_DISPLAY = "tv.988199.xyz"

    data class VideoItem(
        val slot: Int,
        val title: String,
        val videoUrl: String,
        val coverUrl: String?,
        val size: Long
    )

    private val pool = Executors.newFixedThreadPool(4)

    fun <T> async(task: () -> T, done: (T) -> Unit, fail: (Exception) -> Unit) {
        pool.execute {
            try {
                val r = task()
                android.os.Handler(android.os.Looper.getMainLooper()).post { done(r) }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post { fail(e) }
            }
        }
    }

    fun fetchVideos(): List<VideoItem> {
        val text = httpGet("$BASE_URL/api/videos")
        val arr = JSONArray(text)
        val out = ArrayList<VideoItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                VideoItem(
                    slot = o.optInt("slot", 0),
                    title = o.optString("title", ""),
                    videoUrl = o.optString("videoUrl", ""),
                    coverUrl = if (o.isNull("coverUrl")) null else o.optString("coverUrl"),
                    size = o.optLong("size", 0)
                )
            )
        }
        return out
    }

    fun absolute(path: String): String {
        if (path.startsWith("http")) return path
        return BASE_URL + path
    }

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.requestMethod = "GET"
        try {
            if (conn.responseCode !in 200..299) throw RuntimeException("HTTP " + conn.responseCode)
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private val bitmapCache = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun loadBitmap(relPath: String?, targetWidth: Int, onReady: (Bitmap?) -> Unit) {
        if (relPath == null) {
            onReady(null)
            return
        }
        val key = relPath
        val cached = bitmapCache.get(key)
        if (cached != null) {
            onReady(cached)
            return
        }
        async({
            val url = absolute(relPath)
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 15000
            try {
                val ins = conn.inputStream
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                ins.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= targetWidth) sample *= 2
                val conn2 = URL(url).openConnection() as HttpURLConnection
                conn2.connectTimeout = 10000
                conn2.readTimeout = 15000
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bmp = conn2.inputStream.use { BitmapFactory.decodeStream(it, null, opts) }
                if (bmp != null) bitmapCache.put(key, bmp)
                bmp
            } finally {
                conn.disconnect()
            }
        }, { b -> onReady(b) }, { onReady(null) })
    }
}
