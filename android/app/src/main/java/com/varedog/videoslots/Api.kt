package com.varedog.videoslots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
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

    private lateinit var appContext: Context
    private val listPool: ExecutorService = Executors.newSingleThreadExecutor()
    private val imgPool: ExecutorService = Executors.newFixedThreadPool(6)
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun <T> fetchAsync(task: () -> T, done: (T) -> Unit, fail: (Exception) -> Unit) {
        runOn(listPool, task, done, fail)
    }

    fun <T> imgAsync(task: () -> T, done: (T) -> Unit, fail: (T?) -> Unit) {
        runOn(imgPool, task, done, fail)
    }

    private fun <T> runOn(pool: ExecutorService, task: () -> T, done: (T) -> Unit, fail: (Exception) -> Unit) {
        pool.execute {
            try {
                val r = task()
                mainHandler.post { done(r) }
            } catch (e: Exception) {
                mainHandler.post { fail(e) }
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

    private fun cacheDir(): File {
        val d = File(appContext.cacheDir, "covers")
        if (!d.exists()) d.mkdirs()
        return d
    }

    private fun diskFile(relPath: String): File {
        val digest = MessageDigest.getInstance("MD5").digest(relPath.toByteArray())
        val name = digest.joinToString("") { String.format("%02x", it) }
        return File(cacheDir(), "$name.jpg")
    }

    fun loadBitmap(relPath: String?, targetWidth: Int, onReady: (Bitmap?) -> Unit) {
        if (relPath == null) {
            onReady(null)
            return
        }
        val cached = bitmapCache.get(relPath)
        if (cached != null) {
            onReady(cached)
            return
        }
        imgAsync({
            val disk = diskFile(relPath)
            if (disk.exists()) {
                val bmp = decodeBytes(disk.readBytes(), targetWidth)
                if (bmp != null) {
                    bitmapCache.put(relPath, bmp)
                    return@imgAsync bmp
                }
                disk.delete()
            }
            val bytes = downloadBytes(absolute(relPath))
            val bmp = decodeBytes(bytes, targetWidth)
            if (bmp != null) {
                try { disk.writeBytes(bytes) } catch (e: Exception) {}
                bitmapCache.put(relPath, bmp)
            }
            bmp
        }, { b -> onReady(b) }, { onReady(null) })
    }

    private fun downloadBytes(url: String): ByteArray {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 20000
        try {
            if (conn.responseCode !in 200..299) throw RuntimeException("HTTP " + conn.responseCode)
            return conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun decodeBytes(bytes: ByteArray, targetWidth: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= targetWidth) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (e: Exception) {
            null
        }
    }
}
