package com.example.timemap.data

import com.example.timemap.BuildConfig
import com.example.timemap.data.model.HistoricalBuilding
import com.example.timemap.data.model.HistoricalEvent
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * 历史数据仓库（增强版）
 * 提供历史事件和建筑数据
 * 支持本地数据和API数据源
 */
class HistoricalDataRepository {

    // 内存缓存
    private val eventsCache = mutableListOf<HistoricalEvent>()
    private val buildingsCache = mutableListOf<HistoricalBuilding>()

    init {
        // 初始化示例数据
        loadSampleData()
    }

    /**
     * 获取指定位置和年份附近的历史事件
     */
    fun getEventsNearLocation(location: LatLng, year: Int, radiusInKm: Double = 50.0): List<HistoricalEvent> {
        return eventsCache.filter { event ->
            event.isActiveInYear(year) && isWithinRadius(location, event.location, radiusInKm)
        }.sortedByDescending { it.importance }
    }

    /**
     * 获取指定位置和年份附近的历史建筑
     */
    fun getBuildingsNearLocation(location: LatLng, year: Int, radiusInKm: Double = 50.0): List<HistoricalBuilding> {
        return buildingsCache.filter { building ->
            building.existsInYear(year) && isWithinRadius(location, building.location, radiusInKm)
        }
    }

    /**
     * 计算两个地理位置之间的距离（公里）
     */
    private fun isWithinRadius(center: LatLng, target: LatLng, radiusInKm: Double): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            center.latitude, center.longitude,
            target.latitude, target.longitude,
            results
        )
        return results[0] / 1000.0 <= radiusInKm
    }

    /**
     * 加载示例数据
     */
    private fun loadSampleData() {
        // 示例历史事件
        eventsCache.addAll(listOf(
            // 北京地区
            HistoricalEvent(
                id = "event_001",
                title = "秦始皇统一中国",
                description = "秦始皇嬴政统一六国，建立秦朝，定都咸阳",
                year = -221,
                location = LatLng(34.3416, 108.9402),
                locationName = "咸阳",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_002",
                title = "汉朝建立",
                description = "刘邦建立汉朝，定都长安",
                year = -202,
                location = LatLng(34.3416, 108.9402),
                locationName = "长安",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_003",
                title = "唐朝建立",
                description = "李渊建立唐朝，定都长安",
                year = 618,
                location = LatLng(34.3416, 108.9402),
                locationName = "长安",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_004",
                title = "安史之乱",
                description = "安禄山与史思明发动的叛乱，唐朝由盛转衰",
                year = 755,
                endDate = 763,
                location = LatLng(34.3416, 108.9402),
                locationName = "长安",
                category = EventCategory.WAR,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_005",
                title = "宋朝建立",
                description = "赵匡胤陈桥兵变，建立宋朝",
                year = 960,
                location = LatLng(34.8083, 114.6167),
                locationName = "开封",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_006",
                title = "元朝建立",
                description = "忽必烈建立元朝，定都大都（今北京）",
                year = 1271,
                location = LatLng(39.9042, 116.4074),
                locationName = "北京",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_007",
                title = "明朝建立",
                description = "朱元璋建立明朝，定都南京，后迁都北京",
                year = 1368,
                location = LatLng(39.9042, 116.4074),
                locationName = "北京",
                category = EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_008",
                title = "淞沪会战",
                description = "抗日战争中的淞沪会战",
                year = 1937,
                location = LatLng(31.2304, 121.4737),
                locationName = "上海",
                category = EventCategory.WAR,
                importance = 4
            ),
            HistoricalEvent(
                id = "event_009",
                title = "中华人民共和国成立",
                description = "毛泽东在北京天安门宣布中华人民共和国成立",
                year = 1949,
                location = LatLng(39.9042, 116.4074),
                locationName = "北京",
                category = EventCategory.POLITICAL,
                importance = 5
            )
        ))

        // 示例历史建筑
        buildingsCache.addAll(listOf(
            HistoricalBuilding(
                id = "building_001",
                name = "长城",
                description = "中国古代的军事防御工程，世界新七大奇迹之一",
                builtYear = -221,
                location = LatLng(40.4319, 116.5704),
                locationName = "北京",
                architectureStyle = "军事防御建筑",
                status = BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_002",
                name = "故宫（紫禁城）",
                description = "明清两代的皇家宫殿",
                builtYear = 1406,
                endDate = 1420,
                location = LatLng(39.9163, 116.3972),
                locationName = "北京",
                architectureStyle = "中国传统宫殿建筑",
                status = BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_003",
                name = "兵马俑",
                description = "秦始皇陵的陪葬坑，世界文化遗产",
                builtYear = -246,
                location = LatLng(34.3842, 109.2785),
                locationName = "西安",
                architectureStyle = "陵墓建筑",
                status = BuildingStatus.RUINS
            ),
            HistoricalBuilding(
                id = "building_004",
                name = "大雁塔",
                description = "唐代玄奘法师译经之地",
                builtYear = 652,
                location = LatLng(34.2182, 108.9593),
                locationName = "西安",
                architectureStyle = "佛教塔建筑",
                status = BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_005",
                name = "布达拉宫",
                description = "西藏拉萨的宫殿和寺庙建筑群",
                builtYear = 631,
                location = LatLng(29.6554, 91.1176),
                locationName = "拉萨",
                architectureStyle = "藏式宫殿建筑",
                status = BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_006",
                name = "天坛",
                description = "明清两代皇帝祭天祈谷的场所",
                builtYear = 1420,
                location = LatLng(39.8822, 116.4105),
                locationName = "北京",
                architectureStyle = "祭祀建筑",
                status = BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_007",
                name = "颐和园",
                description = "清代皇家园林",
                builtYear = 1750,
                location = LatLng(39.9999, 116.2755),
                locationName = "北京",
                architectureStyle = "皇家园林",
                status = BuildingStatus.EXTANT
            )
        ))
    }
}
