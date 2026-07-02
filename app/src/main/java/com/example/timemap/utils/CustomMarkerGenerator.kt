package com.example.timemap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.timemap.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 自定义标记生成器
 * 用于将景点照片转换为地图标记
 */
class CustomMarkerGenerator(private val context: Context) {

    private val httpClient = OkHttpClient()

    /**
     * 创建带照片的自定义标记
     * @param googleMap GoogleMap实例
     * @param location 标记位置
     * @param title 标记标题
     * @param photoUrl 照片URL（可以是Google Places API的照片引用或网络URL）
     * @param isHistorical 是否为历史建筑（决定标记颜色）
     * @return 创建的Marker对象
     */
    suspend fun createMarkerWithPhoto(
        googleMap: GoogleMap,
        location: LatLng,
        title: String,
        photoUrl: String?,
        snippet: String = "",
        isHistorical: Boolean = false
    ): Marker? = withContext(Dispatchers.IO) {
        try {
            // 下载照片
            val photoBitmap = if (photoUrl != null) {
                downloadBitmap(photoUrl)
            } else {
                null
            }

            // 创建自定义图标
            val icon = if (photoBitmap != null) {
                // 将照片调整为标记大小
                val scaledBitmap = Bitmap.createScaledBitmap(photoBitmap, 100, 100, false)
                BitmapDescriptorFactory.fromBitmap(scaledBitmap)
            } else {
                // 使用默认标记颜色
                if (isHistorical) {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                } else {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
            }

            // 在主线程创建标记
            withContext(Dispatchers.Main) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(title)
                        .snippet(snippet)
                        .icon(icon)
                )
            }
        } catch (e: Exception) {
            Log.e("CustomMarker", "Error creating marker with photo", e)
            null
        }
    }

    /**
     * 下载网络图片
     */
    private suspend fun downloadBitmap(url: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val inputStream = response.body?.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                continuation.resume(bitmap)
            } else {
                continuation.resume(null)
            }
        } catch (e: IOException) {
            Log.e("CustomMarker", "Error downloading bitmap", e)
            continuation.resume(null)
        } catch (e: Exception) {
            Log.e("CustomMarker", "Unexpected error", e)
            continuation.resume(null)
        }
    }

    /**
     * 创建 Ground Overlay（将图片铺在地图上）
     * 适合展示景点示意图、地图等
     * @param googleMap GoogleMap实例
     * @param location 图片中心位置
     * @param imageUrl 图片URL
     * @param widthInMeters 图片宽度（米）
     * @return GroundOverlay对象
     */
    suspend fun createGroundOverlay(
        googleMap: GoogleMap,
        location: LatLng,
        imageUrl: String,
        widthInMeters: Double = 100.0
    ): com.google.android.gms.maps.model.GroundOverlay? = withContext(Dispatchers.IO) {
        try {
            // 下载图片
            val bitmap = downloadBitmap(imageUrl) ?: return@withContext null

            // 在主线程创建Ground Overlay
            withContext(Dispatchers.Main) {
                val bounds = com.google.android.gms.maps.model.LatLngBounds(
                    LatLng(location.latitude - 0.001, location.longitude - 0.001),
                    LatLng(location.latitude + 0.001, location.longitude + 0.001)
                )

                googleMap.addGroundOverlay(
                    com.google.android.gms.maps.model.GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .positionFromBounds(bounds)
                        .transparency(0.2f)  // 设置透明度
                )
            }
        } catch (e: Exception) {
            Log.e("CustomMarker", "Error creating ground overlay", e)
            null
        }
    }

    /**
     * 为景点创建带照片的标记（使用Google Places API的照片）
     * @param photoReference Google Places API的照片引用
     * @param apiKey Google API Key
     */
    suspend fun createMarkerWithPlacesPhoto(
        googleMap: GoogleMap,
        location: LatLng,
        title: String,
        photoReference: String?,
        apiKey: String,
        snippet: String = ""
    ): Marker? {
        if (photoReference == null) {
            return createMarkerWithPhoto(googleMap, location, title, null, snippet, false)
        }

        // 构建Google Places Photo API的URL
        val photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
            "?maxwidth=400" +
            "&photoreference=$photoReference" +
            "&key=$apiKey"

        return createMarkerWithPhoto(googleMap, location, title, photoUrl, snippet, false)
    }
}
