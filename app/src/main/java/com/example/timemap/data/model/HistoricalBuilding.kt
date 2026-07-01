package com.example.timemap.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * 历史建筑数据类
 * @param id 唯一标识符
 * @param name 建筑名称
 * @param description 建筑描述
 * @param builtYear 建造年份 (负数表示公元前)
 * @param destroyedYear 毁坏年份 (可选，如果建筑已毁)
 * @param location 地理位置
 * @param locationName 地点名称
 * @param architectureStyle 建筑风格
 * @param status 建筑状态 (现存、遗址、已毁等)
 */
data class HistoricalBuilding(
    val id: String,
    val name: String,
    val description: String,
    val builtYear: Int,
    val destroyedYear: Int? = null,
    val location: LatLng,
    val locationName: String,
    val architectureStyle: String,
    val status: BuildingStatus
) {
    /**
     * 检查建筑在指定年份是否存在
     */
    fun existsInYear(year: Int): Boolean {
        return year >= builtYear && (destroyedYear == null || year <= destroyedYear)
    }
}

/**
 * 建筑状态枚举
 */
enum class BuildingStatus(val displayName: String) {
    EXTANT("现存"),           // 仍然存在的建筑
    RUINS("遗址"),            // 遗址/遗迹
    DESTROYED("已毁"),        // 已毁坏
    RESTORED("修复"),         // 修复后的建筑
    REBUILT("重建")           // 重建的建筑
}
