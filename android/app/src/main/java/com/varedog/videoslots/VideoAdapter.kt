package com.varedog.videoslots

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class VideoAdapter(private val activity: Activity) : BaseAdapter() {

    private var items: List<Api.VideoItem> = emptyList()
    private val inflater = LayoutInflater.from(activity)

    fun submit(list: List<Api.VideoItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getCount() = items.size
    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: VH
        val view: View
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_video, parent, false)
            holder = VH(
                cover = view.findViewById(R.id.cover),
                title = view.findViewById(R.id.title),
                badge = view.findViewById(R.id.badge)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as VH
        }
        val item = items[position]
        holder.title.text = item.title
        holder.badge.text = item.slot.toString()
        holder.cover.setImageBitmap(null)
        holder.cover.setTag(R.id.cover, item.coverUrl)
        Api.loadBitmap(item.coverUrl, 480) { bmp ->
            if (item.coverUrl != null && holder.cover.getTag(R.id.cover) == item.coverUrl) {
                if (bmp == null) {
                    holder.cover.setBackgroundColor(Color.parseColor("#21262d"))
                } else {
                    holder.cover.setBackgroundColor(Color.TRANSPARENT)
                    holder.cover.setImageBitmap(bmp)
                }
            }
        }
        return view
    }

    private class VH(val cover: ImageView, val title: TextView, val badge: TextView)
}
