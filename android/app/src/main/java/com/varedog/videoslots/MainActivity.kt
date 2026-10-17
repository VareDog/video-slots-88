package com.varedog.videoslots

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray

class MainActivity : Activity() {

    private lateinit var grid: GridView
    private lateinit var loading: ProgressBar
    private lateinit var status: TextView
    private var items: List<Api.VideoItem> = emptyList()
    private var firstLoadDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grid = findViewById(R.id.grid)
        loading = findViewById(R.id.loading)
        status = findViewById(R.id.status)

        val adapter = VideoAdapter(this)
        grid.adapter = adapter
        grid.onItemClickListener = { _, _, position, _ ->
            if (position in items.indices) openPlayer(position)
        }
        grid.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        loadVideos()
    }

    private fun openPlayer(position: Int) {
        val arr = JSONArray()
        for (v in items) {
            val o = org.json.JSONObject()
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
        loading.visibility = View.VISIBLE
        status.visibility = View.GONE
        Api.async(
            task = { Api.fetchVideos() },
            done = { list ->
                loading.visibility = View.GONE
                firstLoadDone = true
                items = list
                (grid.adapter as VideoAdapter).submit(list)
                if (list.isEmpty()) {
                    status.visibility = View.VISIBLE
                    status.text = "暂无视频\n\n请在网页端上传：\n" + Api.HOST_DISPLAY
                }
            },
            fail = { e ->
                loading.visibility = View.GONE
                firstLoadDone = true
                status.visibility = View.VISIBLE
                status.text = "加载失败：${e.message}\n\n按菜单键重试\n服务器：${Api.HOST_DISPLAY}"
            }
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            loadVideos()
            Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
