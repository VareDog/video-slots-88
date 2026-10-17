package com.varedog.videoslots

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import org.json.JSONArray

class PlayerActivity : Activity() {

    private lateinit var video: VideoView
    private var items: List<Pair<String, String>> = emptyList()
    private var index = 0
    private var prepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        video = findViewById(R.id.video)

        val raw = intent.getStringExtra("items") ?: "[]"
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            items = items + Pair(o.optString("title"), o.optString("videoUrl"))
        }
        index = intent.getIntExtra("index", 0).coerceIn(0, (items.size - 1).coerceAtLeast(0))

        video.setOnPreparedListener { mp ->
            prepared = true
            mp.isLooping = false
            video.start()
        }
        video.setOnCompletionListener {
            if (Prefs.isContinuous(this) && items.isNotEmpty()) {
                if (items.size == 1) {
                    video.seekTo(0)
                    video.start()
                } else {
                    index = (index + 1) % items.size
                    playCurrent()
                }
            } else {
                finish()
            }
        }
        video.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "无法播放该视频（what=$what extra=$extra）\n建议使用 H.264 编码 MP4", Toast.LENGTH_LONG).show()
            finish()
            true
        }

        playCurrent()
    }

    private fun playCurrent() {
        if (index !in items.indices) {
            finish()
            return
        }
        prepared = false
        val url = Api.absolute(items[index].second)
        video.setVideoPath(url)
    }

    private fun togglePlay() {
        if (!prepared) return
        if (video.isPlaying) {
            video.pause()
            Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show()
        } else {
            video.start()
            Toast.makeText(this, "播放", Toast.LENGTH_SHORT).show()
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
            text = "开启后：播放结束自动播放下一个视频；\n只有一个视频时自动循环播放。"
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                togglePlay()
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                showMenu()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (prepared && video.isPlaying) video.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        video.stopPlayback()
    }
}
