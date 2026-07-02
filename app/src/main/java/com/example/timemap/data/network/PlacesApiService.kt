package com.example.timemap.data.network

import android.util.Log
import com.example.timemap.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

/**
 * Google Places API 服务类
 * 用于搜索附近的旅游景点和获取景点信息
 */
class PlacesApiService {
    
    private val httpClient = OkHttpClient()
    private val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
    
    companion object {
        private const val TAG = "PlacesApiService"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/place"
    }
    
    /**
     * 搜索附近的旅游景点
     * @param lat 纬度
     * @param lng 经度
     * @param radius 搜索半径（米），默认5000米
     * @param type 地点类型，默认"tourist_attraction"
     * @return 景点列表
     */
    suspend fun searchNearbyAttractions(
        lat: Double,
        lng: Double,
        radius: Int = 5000,
        type: String = "tourist_attraction"
    ): List<PlaceResult> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/nearbysearch/json?" +
            "location=$lat,$lng" +
            "&radius=$radius" +
            "&type=$type" +
            "&key=$apiKey"
        
        return@withContext try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                parsePlacesResult(json)
            } else {
                Log.e(TAG, "API request failed: ${response.code}")
                emptyList()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            emptyList()
        }
    }
    
    /**
     * 根据关键词搜索景点
     * @param query 搜索关键词
     * @param lat 纬度（可选）
     * @param lng 经度（可选）
     * @param radius 搜索半径（米）
     * @return 景点列表
     */
    suspend fun searchPlacesByQuery(
        query: String,
        lat: Double? = null,
        lng: Double? = null,
        radius: Int = 5000
    ): List<PlaceResult> = withContext(Dispatchers.IO) {
        val locationStr = if (lat != null && lng != null) "location=$lat,$lng&radius=$radius&" else ""
        
        val url = "$BASE_URL/textsearch/json?" +
            locationStr +
            "query=${query.replace(" ", "+")}" +
            "&key=$apiKey"
        
        return@withContext try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                parsePlacesResult(json)
            } else {
                Log.e(TAG, "API request failed: ${response.code}")
                emptyList()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            emptyList()
        }
    }
    
    /**
     * 获取景点的详细信息（包括照片）
     * @param placeId Google Places的place_id
     * @return 景点详细信息
     */
    suspend fun getPlaceDetails(placeId: String): PlaceDetails? = withContext(Dispatchers.IO) {
        val fields = "name,rating,formatted_address,geometry,photos,place_id,types,user_ratings_total,opening_hours,website,formatted_phone_number"
        val url = "$BASE_URL/details/json?" +
            "place_id=$placeId" +
            "&fields=$fields" +
            "&key=$apiKey"
        
        return@withContext try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                parsePlaceDetails(json)
            } else {
                Log.e(TAG, "API request failed: ${response.code}")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            null
        }
    }
    
    /**
     * 获取景点照片的URL
     * @param photoReference 照片引用
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 照片URL
     */
    fun getPhotoUrl(photoReference: String, maxWidth: Int = 400, maxHeight: Int = 400): String {
        return "https://maps.googleapis.com/maps/api/place/photo" +
            "?maxwidth=$maxWidth" +
            "&maxheight=$maxHeight" +
            "&photoreference=$photoReference" +
            "&key=$apiKey"
    }
    
    /**
     * 解析Places API返回的JSON数据
     */
    private fun parsePlacesResult(json: JSONObject): List<PlaceResult> {
        val results = mutableListOf<PlaceResult>()
        
        val status = json.getString("status")
        if (status != "OK") {
            Log.w(TAG, "API returned status: $status")
            return emptyList()
        }
        
        val placesArray = json.getJSONArray("results")
        
        for (i in 0 until placesArray.length()) {
            val placeJson = placesArray.getJSONObject(i)
            
            try {
                val placeId = placeJson.getString("place_id")
                val name = placeJson.getString("name")
                
                val geometry = placeJson.getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                
                val types = mutableListOf<String>()
                val typesArray = placeJson.getJSONArray("types")
                for (j in 0 until typesArray.length()) {
                    types.add(typesArray.getString(j))
                }
                
                val rating = if (placeJson.has("rating")) placeJson.getDouble("rating") else null
                val userRatingsTotal = if (placeJson.has("user_ratings_total")) placeJson.getInt("user_ratings_total") else null
                val address = if (placeJson.has("vicinity")) placeJson.getString("vicinity") else null
                
                val photos = mutableListOf<PlacePhoto>()
                if (placeJson.has("photos")) {
                    val photosArray = placeJson.getJSONArray("photos")
                    for (j in 0 until minOf(photosArray.length(), 3)) { // 最多取3张照片
                        val photoJson = photosArray.getJSONObject(j)
                        val photoRef = photoJson.getString("photo_reference")
                        val width = photoJson.getInt("width")
                        val height = photoJson.getInt("height")
                        photos.add(PlacePhoto(photoRef, width, height))
                    }
                }
                
                results.add(
                    PlaceResult(
                        placeId = placeId,
                        name = name,
                        lat = lat,
                        lng = lng,
                        types = types,
                        rating = rating,
                        userRatingsTotal = userRatingsTotal,
                        address = address,
                        photos = photos
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing place: ${placeJson.getString("name")}", e)
            }
        }
        
        return results
    }
    
    /**
     * 解析景点详细信息
     */
    private fun parsePlaceDetails(json: JSONObject): PlaceDetails? {
        val result = json.getJSONObject("result")
        
        return try {
            val placeId = result.getString("place_id")
            val name = result.getString("name")
            
            val geometry = result.getJSONObject("geometry")
            val location = geometry.getJSONObject("location")
            val lat = location.getDouble("lat")
            val lng = location.getDouble("lng")
            
            val rating = if (result.has("rating")) result.getDouble("rating") else null
            val userRatingsTotal = if (result.has("user_ratings_total")) result.getInt("user_ratings_total") else null
            val address = if (result.has("formatted_address")) result.getString("formatted_address") else null
            val website = if (result.has("website")) result.getString("website") else null
            val phoneNumber = if (result.has("formatted_phone_number")) result.getString("formatted_phone_number") else null
            
            val types = mutableListOf<String>()
            if (result.has("types")) {
                val typesArray = result.getJSONArray("types")
                for (i in 0 until typesArray.length()) {
                    types.add(typesArray.getString(i))
                }
            }
            
            val openingHours = if (result.has("opening_hours")) {
                val hoursJson = result.getJSONObject("opening_hours")
                val weekdayText = mutableListOf<String>()
                if (hoursJson.has("weekday_text")) {
                    val weekdayArray = hoursJson.getJSONArray("weekday_text")
                    for (i in 0 until weekdayArray.length()) {
                        weekdayText.add(weekdayArray.getString(i))
                    }
                }
                PlaceOpeningHours(hoursJson.getBoolean("open_now"), weekdayText)
            } else {
                null
            }
            
            val photos = mutableListOf<PlacePhoto>()
            if (result.has("photos")) {
                val photosArray = result.getJSONArray("photos")
                for (i in 0 until minOf(photosArray.length(), 5)) { // 最多取5张照片
                    val photoJson = photosArray.getJSONObject(i)
                    val photoRef = photoJson.getString("photo_reference")
                    val width = photoJson.getInt("width")
                    val height = photoJson.getInt("height")
                    photos.add(PlacePhoto(photoRef, width, height))
                }
            }
            
            PlaceDetails(
                placeId = placeId,
                name = name,
                lat = lat,
                lng = lng,
                types = types,
                rating = rating,
                userRatingsTotal = userRatingsTotal,
                address = address,
                website = website,
                phoneNumber = phoneNumber,
                openingHours = openingHours,
                photos = photos
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing place details", e)
            null
        }
    }
}

/**
 * 景点搜索结果数据类
 */
data class PlaceResult(
    val placeId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val types: List<String>,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val address: String?,
    val photos: List<PlacePhoto>
)

/**
 * 景点详细信息数据类
 */
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val types: List<String>,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val address: String?,
    val website: String?,
    val phoneNumber: String?,
    val openingHours: PlaceOpeningHours?,
    val photos: List<PlacePhoto>
)

/**
 * 景点照片数据类
 */
data class PlacePhoto(
    val photoReference: String,
    val width: Int,
    val height: Int
)

/**
 * 景点营业时间数据类
 */
data class PlaceOpeningHours(
    val openNow: Boolean,
    val weekdayText: List<String>
)
