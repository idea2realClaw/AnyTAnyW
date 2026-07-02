package com.example.timemap.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timemap.R
import com.example.timemap.data.HistoricalDataRepository
import com.example.timemap.data.model.HistoricalBuilding
import com.example.timemap.data.model.HistoricalEvent
import com.example.timemap.data.network.PlacesApiService
import com.example.timemap.utils.CustomMarkerGenerator
import com.example.timemap.views.LogarithmicTimelineSlider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var timelineSlider: LogarithmicTimelineSlider
    private lateinit var tvSelectedYear: TextView
    private lateinit var tvEventCount: TextView
    private lateinit var etEarliestYear: TextInputEditText
    private lateinit var btnSetYear: Button
    private lateinit var tvEarliestYearLabel: TextView
    private lateinit var tvCurrentYearLabel: TextView
    private lateinit var fabSearchAttractions: FloatingActionButton

    private val dataRepository = HistoricalDataRepository()
    private val placesApiService = PlacesApiService()
    private lateinit var markerGenerator: CustomMarkerGenerator
    private val eventMarkers = mutableListOf<Marker>()
    private val buildingMarkers = mutableListOf<Marker>()
    private val attractionMarkers = mutableListOf<Marker>()

    // 协程作用域
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 默认位置：北京
    private val defaultLocation = LatLng(39.9042, 116.4074)
    private val defaultZoom = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initViews()
        setupMap()
        setupTimelineSlider()
        setupEarliestYearInput()
        setupSearchAttractionsButton()
    }

    private fun initViews() {
        timelineSlider = findViewById(R.id.timelineSlider)
        tvSelectedYear = findViewById(R.id.tvSelectedYear)
        tvEventCount = findViewById(R.id.tvEventCount)
        etEarliestYear = findViewById(R.id.etEarliestYear)
        btnSetYear = findViewById(R.id.btnSetYear)
        tvEarliestYearLabel = findViewById(R.id.tvEarliestYearLabel)
        tvCurrentYearLabel = findViewById(R.id.tvCurrentYearLabel)
        fabSearchAttractions = findViewById(R.id.fabSearchAttractions)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupTimelineSlider() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        timelineSlider.earliestYear = -221
        timelineSlider.latestYear = currentYear
        timelineSlider.selectedYear = currentYear

        updateYearDisplay(currentYear)
        updateTimelineLabels()

        // 年份变化监听
        timelineSlider.onYearChangedListener = { year ->
            runOnUiThread {
                updateYearDisplay(year)
                loadHistoricalDataForYear(year)
            }
        }
    }

    private fun setupEarliestYearInput() {
        btnSetYear.setOnClickListener {
            val input = etEarliestYear.text.toString().trim()
            if (input.isNotEmpty()) {
                try {
                    val year = input.toInt()
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                    if (year >= currentYear) {
                        Toast.makeText(this, "最早年份不能晚于当前年份", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    timelineSlider.earliestYear = year
                    updateTimelineLabels()
                    loadHistoricalDataForYear(timelineSlider.selectedYear)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "请输入有效的年份", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearchAttractionsButton() {
        fabSearchAttractions.setOnClickListener {
            if (!::googleMap.isInitialized) return@setOnClickListener

            val currentLocation = googleMap.cameraPosition.target
            searchNearbyAttractions(currentLocation.latitude, currentLocation.longitude)
        }
    }

    private fun updateYearDisplay(year: Int) {
        val yearText = if (year < 0) {
            "${Math.abs(year)} BC"
        } else {
            "$year"
        }
        tvSelectedYear.text = getString(R.string.year_label) + yearText
    }

    private fun updateTimelineLabels() {
        val earliestText = if (timelineSlider.earliestYear < 0) {
            "${Math.abs(timelineSlider.earliestYear)} BC"
        } else {
            "${timelineSlider.earliestYear}"
        }

        tvEarliestYearLabel.text = earliestText
        tvCurrentYearLabel.text = "${timelineSlider.latestYear}"
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        markerGenerator = CustomMarkerGenerator(this)

        // 配置地图
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false

        // 设置自定义信息窗口适配器
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        // 移动到默认位置
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, defaultZoom))

        // 加载当前年份的数据
        loadHistoricalDataForYear(timelineSlider.selectedYear)

        // 地图点击事件
        googleMap.setOnMapClickListener { latLng ->
            // 可以在这里添加功能：点击地图显示该位置的年份信息
        }

        // 标记点击事件：显示详细信息
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    /**
     * 搜索附近的旅游景点
     */
    private fun searchNearbyAttractions(lat: Double, lng: Double) {
        Toast.makeText(this, "正在搜索附近景点...", Toast.LENGTH_SHORT).show()

        activityScope.launch {
            try {
                val attractions = withContext(Dispatchers.IO) {
                    placesApiService.searchNearbyAttractions(lat, lng, radius = 5000)
                }

                if (attractions.isEmpty()) {
                    Toast.makeText(this@MapsActivity, "未找到附近景点", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 清除旧的景点标记
                attractionMarkers.forEach { it.remove() }
                attractionMarkers.clear()

                // 在地图上添加景点标记
                for (attraction in attractions) {
                    addAttractionMarker(attraction)
                }

                Toast.makeText(this@MapsActivity, "找到 ${attractions.size} 个景点", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MapsActivity, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 添加景点标记到地图
     */
    private suspend fun addAttractionMarker(attraction: com.example.timemap.data.network.PlaceResult) {
        val location = LatLng(attraction.lat, attraction.lng)

        // 如果有照片，使用照片作为标记
        if (attraction.photos.isNotEmpty()) {
            val photoRef = attraction.photos[0].photoReference
            val photoUrl = placesApiService.getPhotoUrl(photoRef)

            val marker = markerGenerator.createMarkerWithPhoto(
                googleMap = googleMap,
                location = location,
                title = attraction.name,
                photoUrl = photoUrl,
                snippet = "${attraction.address ?: ""} - 评分: ${attraction.rating ?: "无"}",
                isHistorical = false
            )

            marker?.let {
                it.tag = attraction
                attractionMarkers.add(it)
            }
        } else {
            // 没有照片，使用默认标记
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(attraction.name)
                    .snippet("${attraction.address ?: ""} - 评分: ${attraction.rating ?: "无"}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            marker?.let {
                it.tag = attraction
                attractionMarkers.add(it)
            }
        }
    }

    private fun loadHistoricalDataForYear(year: Int) {
        if (!::googleMap.isInitialized) return

        // 清除旧的标记（保留景点标记）
        eventMarkers.forEach { it.remove() }
        buildingMarkers.forEach { it.remove() }
        eventMarkers.clear()
        buildingMarkers.clear()

        val currentLocation = googleMap.cameraPosition.target
        val zoomLevel = googleMap.cameraPosition.zoom

        // 根据缩放级别调整搜索半径
        val radiusInKm = when {
            zoomLevel > 10 -> 10.0   // 城市级别
            zoomLevel > 7 -> 50.0    // 区域级别
            zoomLevel > 5 -> 200.0   // 国家级别
            else -> 500.0            // 洲际级别
        }

        // 获取历史事件
        val events = dataRepository.getEventsNearLocation(currentLocation, year, radiusInKm)
        val buildings = dataRepository.getBuildingsNearLocation(currentLocation, year, radiusInKm)

        // 更新事件计数显示
        val totalCount = events.size + buildings.size
        tvEventCount.text = "找到 $totalCount 个历史事件/建筑 | 景点: ${attractionMarkers.size}个"

        // 在地图上添加事件标记
        events.forEach { event ->
            addEventMarker(event)
        }

        // 在地图上添加建筑标记
        buildings.forEach { building ->
            addBuildingMarker(building)
        }

        // 如果没有找到数据，显示提示
        if (totalCount == 0 && attractionMarkers.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_events), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addEventMarker(event: HistoricalEvent) {
        val markerOptions = MarkerOptions()
            .position(event.location)
            .title(event.title)
            .snippet("${event.locationName} - ${event.description}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        val marker = googleMap.addMarker(markerOptions)
        marker?.let {
            it.tag = event
            eventMarkers.add(it)
        }
    }

    private fun addBuildingMarker(building: HistoricalBuilding) {
        val markerOptions = MarkerOptions()
            .position(building.location)
            .title(building.name)
            .snippet("${building.locationName} - ${building.architectureStyle}")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        val marker = googleMap.addMarker(markerOptions)
        marker?.let {
            it.tag = building
            buildingMarkers.add(it)
        }
    }

    private fun clearAllMarkers() {
        eventMarkers.forEach { it.remove() }
        buildingMarkers.forEach { it.remove() }
        attractionMarkers.forEach { it.remove() }
        eventMarkers.clear()
        buildingMarkers.clear()
        attractionMarkers.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllMarkers()
        activityScope.cancel() // 取消所有协程
    }
}
