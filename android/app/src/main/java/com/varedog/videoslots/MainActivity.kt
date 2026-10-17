package com.varedog.videoslots

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var grid: GridView
    private lateinit var loading: ProgressBar
    private lateinit var status: TextView
    private var items: List<Api.VideoItem> = emptyList()
    private var lastJson = ""
    private var lastSelection = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grid = findViewById(R.id.grid)
        loading = findViewById(R.id.loading)
        status = findViewById(R.id.status)

        val adapter = VideoAdapter(this)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position in items.indices) openPlayer(position)
        }
        grid.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                lastSelection = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        grid.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        val cached = Prefs.loadListCache(this)
        if (cached != null) {
            items = cached
            lastJson = serialize(cached)
            (grid.adapter as VideoAdapter).submit(cached)
            loading.visibility = View.GONE
            if (cached.isEmpty()) showEmpty()
            restoreSelection()
        }
        loadVideos()
    }

    private fun restoreSelection() {
        if (items.isNotEmpty()) {
            grid.setSelection(lastSelection.coerceIn(0, items.size - 1))
        }
    }

    private fun serialize(list: List<Api.VideoItem>): String {
        val arr = JSONArray()
        for (v in list) {
            val o = JSONObject()
            o.put("slot", v.slot)
            o.put("title", v.title)
            o.put("videoUrl", v.videoUrl)
            o.put("coverUrl", v.coverUrl ?: JSONObject.NULL)
            o.put("size", v.size)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun openPlayer(position: Int) {
        val arr = JSONArray()
        for (v in items) {
            val o = JSONObject()
            o.put("title", v.title)
            o.put("videoUrl", v.videoUrl)
            arr.put(o)
        }
        val it = Intent(this, PlayerActivity::class.java)
        it.putExtra("items", arr.toString())
        it.putExtra("index", position)
        startActivity(it)
    }

    private fun loadVideos() {
        if (items.isEmpty()) loading.visibility = View.VISIBLE
        status.visibility = View.GONE
        Api.fetchAsync(
            task = { Api.fetchVideos() },
            done = { list ->
                loading.visibility = View.GONE
                val json = serialize(list)
                if (json != lastJson) {
                    lastJson = json
                    items = list
                    Prefs.saveListCache(this, list)
                    (grid.adapter as VideoAdapter).submit(list)
                    restoreSelection()
                }
                if (list.isEmpty()) showEmpty()
            },
            fail = { e ->
                loading.visibility = View.GONE
                if (items.isEmpty()) {
                    status.visibility = View.VISIBLE
                    status.text = "加载失败：${e.message}\n\n按菜单键重试\n服务器：${Api.HOST_DISPLAY}"
                } else {
                    Toast.makeText(this, "刷新失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showEmpty() {
        status.visibility = View.VISIBLE
        status.text = "暂无视频\n\n请在网页端上传：\n" + Api.HOST_DISPLAY
    }

    private fun showMenu() {
        val box = CheckBox(this).apply {
            text = "启用连续播放"
            isChecked = Prefs.isContinuous(this@MainActivity)
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
                Prefs.setContinuous(this@MainActivity, box.isChecked)
                Toast.makeText(
                    this,
                    if (box.isChecked) "已启用连续播放" else "已关闭连续播放",
                    Toast.LENGTH_SHORT
                ).show()
                d.dismiss()
            }
            .setNeutralButton("刷新列表") { _, _ ->
                loadVideos()
                Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                showMenu()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val pos = grid.selectedItemPosition
                if (pos != AdapterView.INVALID_POSITION && pos in items.indices) {
                    openPlayer(pos)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
