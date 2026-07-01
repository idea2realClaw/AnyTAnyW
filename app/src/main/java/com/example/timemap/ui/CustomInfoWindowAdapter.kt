package com.example.timemap.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.timemap.R
import com.example.timemap.data.model.HistoricalBuilding
import com.example.timemap.data.model.HistoricalEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * 自定义地图标记信息窗口适配器
 * 用于显示历史事件和建筑的详细信息
 */
class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getInfoWindow(marker: Marker): View? {
        return null // 返回null使用默认窗口框架
    }

    override fun getInfoContents(marker: Marker): View {
        val view = inflater.inflate(R.layout.custom_info_window, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvYear = view.findViewById<TextView>(R.id.tvYear)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)

        val tag = marker.tag

        when (tag) {
            is HistoricalEvent -> {
                // 设置事件信息
                tvTitle.text = tag.title
                tvYear.text = formatYear(tag.year)
                tvLocation.text = tag.locationName
                tvDescription.text = tag.description
                tvCategory.text = tag.category.displayName
                tvCategory.setBackgroundColor(tag.category.color)
            }

            is HistoricalBuilding -> {
                // 设置建筑信息
                tvTitle.text = tag.name
                tvYear.text = "建造: ${formatYear(tag.builtYear)}"
                tvLocation.text = tag.locationName
                tvDescription.text = tag.description
                tvCategory.text = "建筑 - ${tag.status.displayName}"
                tvCategory.setBackgroundColor(0xFF4CAF50.toInt()) // 绿色
            }

            else -> {
                // 默认显示
                tvTitle.text = marker.title ?: "未知"
                tvYear.text = ""
                tvLocation.text = marker.snippet ?: ""
                tvDescription.text = ""
                tvCategory.text = ""
                tvCategory.visibility = View.GONE
            }
        }

        return view
    }

    private fun formatYear(year: Int): String {
        return if (year < 0) {
            "${Math.abs(year)} BC (公元前)"
        } else {
            "$year AD (公元)"
        }
    }
}
