package com.example.timemap.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timemap.R
import com.example.timemap.data.HistoricalDataRepository
import com.example.timemap.data.model.HistoricalEvent
import com.example.timemap.data.model.HistoricalBuilding
import com.example.timemap.views.LogarithmicTimelineSlider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.TextInputEditText
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

    private val dataRepository = HistoricalDataRepository()
    private val eventMarkers = mutableListOf<Marker>()
    private val buildingMarkers = mutableListOf<Marker>()

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
    }

    private fun initViews() {
        timelineSlider = findViewById(R.id.timelineSlider)
        tvSelectedYear = findViewById(R.id.tvSelectedYear)
        tvEventCount = findViewById(R.id.tvEventCount)
        etEarliestYear = findViewById(R.id.etEarliestYear)
        btnSetYear = findViewById(R.id.btnSetYear)
        tvEarliestYearLabel = findViewById(R.id.tvEarliestYearLabel)
        tvCurrentYearLabel = findViewById(R.id.tvCurrentYearLabel)
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

        // 地图点击事件：可以后续扩展显示该位置的历史信息
        googleMap.setOnMapClickListener { latLng ->
            // 可以在这里添加功能：点击地图显示该位置的年份信息
        }

        // 标记点击事件：显示详细信息
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }

    private fun loadHistoricalDataForYear(year: Int) {
        if (!::googleMap.isInitialized) return

        // 清除旧的标记
        clearAllMarkers()

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
        tvEventCount.text = "找到 $totalCount 个历史事件/建筑"

        // 在地图上添加事件标记
        events.forEach { event ->
            addEventMarker(event)
        }

        // 在地图上添加建筑标记
        buildings.forEach { building ->
            addBuildingMarker(building)
        }

        // 如果没有找到数据，显示提示
        if (totalCount == 0) {
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
        eventMarkers.clear()
        buildingMarkers.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllMarkers()
    }
}
