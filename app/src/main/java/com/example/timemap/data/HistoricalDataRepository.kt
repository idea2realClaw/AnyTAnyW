package com.example.timemap.data

import com.example.timemap.data.model.BuildingStatus
import com.example.timemap.data.model.EventCategory
import com.example.timemap.data.model.HistoricalBuilding
import com.example.timemap.data.model.HistoricalEvent
import com.google.android.gms.maps.model.LatLng

/**
 * 历史数据仓库
 * 提供历史事件和建筑数据
 * 当前使用本地示例数据，可扩展为API数据源
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
     * @param location 中心位置
     * @param year 目标年份
     * @param radiusInKm 搜索半径（公里）
     * @return 匹配的事件列表
     */
    fun getEventsNearLocation(location: LatLng, year: Int, radiusInKm: Double = 50.0): List<HistoricalEvent> {
        // TODO: 后续可替换为API调用
        // 示例: apiService.getHistoricalEvents(location.latitude, location.longitude, year, radiusInKm)

        return eventsCache.filter { event ->
            event.isActiveInYear(year) && isWithinRadius(location, event.location, radiusInKm)
        }.sortedByDescending { it.importance }
    }

    /**
     * 获取指定位置和年份附近的历史建筑
     * @param location 中心位置
     * @param year 目标年份
     * @param radiusInKm 搜索半径（公里）
     * @return 匹配的建筑列表
     */
    fun getBuildingsNearLocation(location: LatLng, year: Int, radiusInKm: Double = 50.0): List<HistoricalBuilding> {
        // TODO: 后续可替换为API调用
        // 示例: apiService.getHistoricalBuildings(location.latitude, location.longitude, year, radiusInKm)

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
     * 包含中国历史重要事件和建筑
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
                location = LatLng(34.3416, 108.9402),  // 咸阳
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
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
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
                category = com.example.timemap.data.model.EventCategory.WAR,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_005",
                title = "宋朝建立",
                description = "赵匡胤陈桥兵变，建立宋朝",
                year = 960,
                location = LatLng(34.8083, 114.6167),  // 开封
                locationName = "开封",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_006",
                title = "元朝建立",
                description = "忽必烈建立元朝，定都大都（今北京）",
                year = 1271,
                location = LatLng(39.9042, 116.4074),
                locationName = "北京",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_007",
                title = "明朝建立",
                description = "朱元璋建立明朝，定都南京，后迁都北京",
                year = 1368,
                location = LatLng(39.9042, 116.4074),
                locationName = "北京",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_008",
                title = "清朝建立",
                description = "皇太极改国号为清",
                year = 1636,
                location = LatLng(41.8057, 123.4315),  // 沈阳
                locationName = "沈阳",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_009",
                title = "鸦片战争",
                description = "第一次鸦片战争爆发",
                year = 1840,
                location = LatLng(22.5431, 114.0579),  // 广州
                locationName = "广州",
                category = com.example.timemap.data.model.EventCategory.WAR,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_010",
                title = "辛亥革命",
                description = "辛亥革命爆发，推翻清朝统治",
                year = 1911,
                location = LatLng(30.5928, 114.3055),  // 武汉
                locationName = "武汉",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            // 世界历史事件
            HistoricalEvent(
                id = "event_101",
                title = "罗马帝国建立",
                description = "屋大维建立罗马帝国",
                year = -27,
                location = LatLng(41.9028, 12.4964),
                locationName = "罗马",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_102",
                title = "文艺复兴开始",
                description = "欧洲文艺复兴运动兴起",
                year = 1300,
                endDate = 1600,
                location = LatLng(43.7696, 11.2558),
                locationName = "佛罗伦萨",
                category = com.example.timemap.data.model.EventCategory.CULTURE,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_103",
                title = "美国独立宣言",
                description = "北美十三州宣布独立",
                year = 1776,
                location = LatLng(38.9072, -77.0369),
                locationName = "费城",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_104",
                title = "法国大革命",
                description = "法国爆发资产阶级革命",
                year = 1789,
                endDate = 1799,
                location = LatLng(48.8566, 2.3522),
                locationName = "巴黎",
                category = com.example.timemap.data.model.EventCategory.POLITICAL,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_105",
                title = "第一次世界大战",
                description = "第一次世界大战爆发",
                year = 1914,
                endDate = 1918,
                location = LatLng(48.8566, 2.3522),
                locationName = "欧洲",
                category = com.example.timemap.data.model.EventCategory.WAR,
                importance = 5
            ),
            HistoricalEvent(
                id = "event_106",
                title = "第二次世界大战",
                description = "第二次世界大战",
                year = 1939,
                endDate = 1945,
                location = LatLng(48.8566, 2.3522),
                locationName = "全球",
                category = com.example.timemap.data.model.EventCategory.WAR,
                importance = 5
            )
        ))

        // 示例历史建筑
        buildingsCache.addAll(listOf(
            // 中国建筑
            HistoricalBuilding(
                id = "building_001",
                name = "长城",
                description = "中国古代的军事防御工程，世界新七大奇迹之一",
                builtYear = -221,  // 始建于秦朝
                location = LatLng(40.4319, 116.5704),  // 八达岭长城
                locationName = "北京",
                architectureStyle = "军事防御建筑",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
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
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_003",
                name = "兵马俑",
                description = "秦始皇陵的陪葬坑，世界文化遗产",
                builtYear = -246,
                location = LatLng(34.3842, 109.2785),
                locationName = "西安",
                architectureStyle = "陵墓建筑",
                status = com.example.timemap.data.model.BuildingStatus.RUINS
            ),
            HistoricalBuilding(
                id = "building_004",
                name = "大雁塔",
                description = "唐代玄奘法师译经之地",
                builtYear = 652,
                location = LatLng(34.2182, 108.9593),
                locationName = "西安",
                architectureStyle = "佛教塔建筑",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_005",
                name = "布达拉宫",
                description = "西藏拉萨的宫殿和寺庙建筑群",
                builtYear = 631,
                location = LatLng(29.6554, 91.1176),
                locationName = "拉萨",
                architectureStyle = "藏式宫殿建筑",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            ),
            // 世界建筑
            HistoricalBuilding(
                id = "building_101",
                name = "金字塔",
                description = "古埃及法老的陵墓",
                builtYear = -2580,
                location = LatLng(29.9792, 31.1342),
                locationName = "吉萨",
                architectureStyle = "古埃及陵墓建筑",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_102",
                name = "帕特农神庙",
                description = "古希腊雅典娜女神神庙",
                builtYear = -447,
                endDate = -432,
                location = LatLng(37.9715, 23.7267),
                locationName = "雅典",
                architectureStyle = "古希腊神庙建筑",
                status = com.example.timemap.data.model.BuildingStatus.RUINS
            ),
            HistoricalBuilding(
                id = "building_103",
                name = "罗马斗兽场",
                description = "古罗马最大的圆形角斗场",
                builtYear = 72,
                endDate = 80,
                location = LatLng(41.8902, 12.4922),
                locationName = "罗马",
                architectureStyle = "古罗马竞技场建筑",
                status = com.example.timemap.data.model.BuildingStatus.RUINS
            ),
            HistoricalBuilding(
                id = "building_104",
                name = "巴黎圣母院",
                description = "法国哥特式教堂",
                builtYear = 1163,
                endDate = 1345,
                location = LatLng(48.8530, 2.3499),
                locationName = "巴黎",
                architectureStyle = "哥特式教堂",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            ),
            HistoricalBuilding(
                id = "building_105",
                name = "埃菲尔铁塔",
                description = "巴黎地标建筑",
                builtYear = 1887,
                endDate = 1889,
                location = LatLng(48.8584, 2.2945),
                locationName = "巴黎",
                architectureStyle = "铁塔建筑",
                status = com.example.timemap.data.model.BuildingStatus.EXTANT
            )
        ))
    }

    /**
     * TODO: API接口预留
     * 后续可以添加从网络API获取数据的功能
     */

    interface HistoricalDataApiService {
        suspend fun getHistoricalEvents(lat: Double, lng: Double, year: Int, radius: Double): List<HistoricalEvent>
        suspend fun getHistoricalBuildings(lat: Double, lng: Double, year: Int, radius: Double): List<HistoricalBuilding>
    }
}
