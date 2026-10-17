package com.varedog.videoslots

import android.app.Activity
import android.app.AlertDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray

class PlayerActivity : Activity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var buffering: FrameLayout
    private lateinit var bufferText: TextView
    private lateinit var titleView: TextView

    private var items: List<Pair<String, String>> = emptyList()
    private var index = 0
    private var surface: Surface? = null

    private var current: MediaPlayer? = null
    private var currentReady = false
    private var next: MediaPlayer? = null
    private var nextIndex = -1
    private var nextReady = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        surfaceView = findViewById(R.id.surface)
        buffering = findViewById(R.id.buffering)
        bufferText = findViewById(R.id.bufferText)
        titleView = findViewById(R.id.titleView)

        val raw = intent.getStringExtra("items") ?: "[]"
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            items = items + Pair(o.optString("title"), o.optString("videoUrl"))
        }
        index = intent.getIntExtra("index", 0).coerceIn(0, (items.size - 1).coerceAtLeast(0))

        surfaceView.holder.addCallback(this)
        surfaceView.holder.setKeepScreenOn(true)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
        val c = current
        if (c == null) {
            playIndex(index)
        } else {
            try { c.setSurface(surface) } catch (e: Exception) {}
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
    }

    private fun playIndex(i: Int) {
        if (i !in items.indices) {
            finish()
            return
        }
        index = i
        showTitle()
        currentReady = false
        showBuffering("加载中...")
        val mp = MediaPlayer()
        try {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mp.setDataSource(Api.absolute(items[i].second))
            if (surface != null) mp.setSurface(surface)
        } catch (e: Exception) {
            fail("打开视频失败: ${e.message}")
            return
        }
        mp.setOnPreparedListener { p ->
            currentReady = true
            if (items.size == 1 && Prefs.isContinuous(this)) {
                p.isLooping = true
            }
            p.start()
            hideBuffering()
            schedulePrefetch()
        }
        mp.setOnCompletionListener { onCompleted() }
        mp.setOnErrorListener { _, what, extra ->
            fail("播放失败（$what/$extra）\n建议上传 H.264 编码 MP4")
            true
        }
        mp.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> showBuffering("缓冲中...")
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> hideBuffering()
            }
            true
        }
        mp.setOnBufferingUpdateListener { _, pct ->
            if (buffering.visibility == View.VISIBLE) {
                bufferText.text = "缓冲中 $pct%"
            }
        }
        current = mp
        mp.prepareAsync()
    }

    private fun onCompleted() {
        if (Prefs.isContinuous(this) && items.isNotEmpty()) {
            val n = if (items.size == 1) index else (index + 1) % items.size
            swapOrPlay(n)
        } else {
            finish()
        }
    }

    private fun swapOrPlay(n: Int) {
        val pre = next
        if (pre != null && nextIndex == n && nextReady) {
            try { current?.setSurface(null) } catch (e: Exception) {}
            try { current?.release() } catch (e: Exception) {}
            current = pre
            next = null
            nextIndex = -1
            nextReady = false
            currentReady = true
            index = n
            showTitle()
            try { if (surface != null) current?.setSurface(surface) } catch (e: Exception) {}
            current?.start()
            schedulePrefetch()
        } else {
            try { current?.setSurface(null) } catch (e: Exception) {}
            try { current?.release() } catch (e: Exception) {}
            current = null
            playIndex(n)
        }
    }

    private fun schedulePrefetch() {
        if (items.size <= 1) return
        if (!Prefs.isContinuous(this)) return
        val n = (index + 1) % items.size
        if (next != null && nextIndex == n) return
        releaseNext()
        nextIndex = n
        nextReady = false
        val mp = MediaPlayer()
        try {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mp.setDataSource(Api.absolute(items[n].second))
        } catch (e: Exception) {
            next = null
            nextIndex = -1
            return
        }
        mp.setOnPreparedListener { nextReady = true }
        mp.setOnErrorListener { _, _, _ ->
            releaseNext()
            true
        }
        next = mp
        mp.prepareAsync()
    }

    private fun releaseNext() {
        try { next?.release() } catch (e: Exception) {}
        next = null
        nextIndex = -1
        nextReady = false
    }

    private fun togglePlay() {
        val c = current ?: return
        if (!currentReady) return
        try {
            if (c.isPlaying) {
                c.pause()
                Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show()
            } else {
                c.start()
                Toast.makeText(this, "播放", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
        }
    }

    private fun showMenu() {
        val box = CheckBox(this).apply {
            text = "启用连续播放"
            isChecked = Prefs.isContinuous(this@PlayerActivity)
            textSize = 17f
            setPadding(48, 24, 24, 24)
        }
        val tip = TextView(this).apply {
            text = "开启后：播放结束自动播放下一个视频；\n下一个视频会提前预加载，切换更快；\n只有一个视频时自动循环播放。"
            textSize = 13f
            setPadding(48, 0, 48, 16)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(tip)
            addView(box)
        }
        AlertDialog.Builder(this)
            .setTitle("菜单")
            .setView(container)
            .setPositiveButton("确定") { d, _ ->
                Prefs.setContinuous(this@PlayerActivity, box.isChecked)
                if (box.isChecked) {
                    schedulePrefetch()
                } else {
                    releaseNext()
                }
                Toast.makeText(
                    this,
                    if (box.isChecked) "已启用连续播放" else "已关闭连续播放",
                    Toast.LENGTH_SHORT
                ).show()
                d.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showTitle() {
        titleView.text = items.getOrNull(index)?.first ?: ""
        titleView.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages(TITLE_TOKEN)
        handler.postDelayed({ titleView.visibility = View.GONE }, TITLE_TOKEN, 2600)
    }

    private fun showBuffering(text: String) {
        bufferText.text = text
        buffering.visibility = View.VISIBLE
    }

    private fun hideBuffering() {
        buffering.visibility = View.GONE
    }

    private fun fail(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when {
            Keys.isConfirm(keyCode) -> {
                togglePlay()
                return true
            }
            keyCode == KeyEvent.KEYCODE_MENU -> {
                showMenu()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (Keys.isConfirm(keyCode)) return true
        return super.onKeyUp(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        val c = current
        try {
            if (c != null && currentReady && c.isPlaying) c.pause()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        try { current?.release() } catch (e: Exception) {}
        try { next?.release() } catch (e: Exception) {}
        current = null
        next = null
    }

    companion object {
        private val TITLE_TOKEN = Any()
    }
}
