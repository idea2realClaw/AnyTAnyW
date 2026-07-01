package com.example.timemap.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * 历史事件数据类
 * @param id 唯一标识符
 * @param title 事件标题
 * @param description 事件描述
 * @param year 发生年份 (负数表示公元前)
 * @param endDate 结束年份 (可选，用于持续时间较长的事件)
 * @param location 地理位置
 * @param locationName 地点名称
 * @param category 事件类别 (政治、战争、文化、科技等)
 * @param importance 重要性等级 (1-5, 5为最重要)
 */
data class HistoricalEvent(
    val id: String,
    val title: String,
    val description: String,
    val year: Int,
    val endDate: Int? = null,
    val location: LatLng,
    val locationName: String,
    val category: EventCategory,
    val importance: Int = 3
) {
    /**
     * 检查事件是否在指定年份活跃
     */
    fun isActiveInYear(year: Int): Boolean {
        return if (endDate != null) {
            year >= this.year && year <= endDate
        } else {
            year == this.year
        }
    }
}

/**
 * 事件类别枚举
 */
enum class EventCategory(val displayName: String, val color: Int) {
    POLITICAL("政治", 0xFF1565C0.toInt()),      // 蓝色
    WAR("战争", 0xFFC62828.toInt()),             // 红色
    CULTURE("文化", 0xFF6A1B9A.toInt()),         // 紫色
    SCIENCE("科技", 0xFF2E7D32.toInt()),         // 绿色
    RELIGION("宗教", 0xFFFF8F00.toInt()),        // 橙色
    ARCHITECTURE("建筑", 0xFF00838F.toInt()),    // 青色
    DISCOVERY("探索", 0xFFAD1457.toInt()),       // 粉红
    ECONOMY("经济", 0xFF558B2F.toInt())          // 浅绿
}
