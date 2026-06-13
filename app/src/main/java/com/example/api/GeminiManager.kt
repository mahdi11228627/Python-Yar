package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiManager {

    private const val TAG = "GeminiManager"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚠️ کلید دسترسی Gemini API تنظیم نشده است.\nلطفاً در پنل Secrets در گوگل آی‌استودیو متغیر GEMINI_API_KEY را با یک کلید معتبر پر کنید تا موتور هوشمند هویت بیابد."
        }
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contentsArray)
            
            val systemInstructionObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", "شما «پایتون‌یار»، یک دستیار هوشمند، صمیمی و مهربان برای آموزش مفاهیم پایتون به مبتدی‌ها هستید. پیام‌ها را شفاف، گام‌به‌گام و با زبان فارسی شیوا پاسخ دهید. از فونت مونو اسپیس یا نشانه‌گذاری‌های خوانا برای کدهای پایتون استفاده کنید. پاسخ‌های خود را با لحنی خودمانی، ترغیب‌کننده و مدرن ارائه دهید.")
                    })
                })
            }
            put("systemInstruction", systemInstructionObj)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
            
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code=${response.code}, Body=$errBody")
                    return@withContext "❌ خطا در دریافت پاسخ از پایتون‌یار (کد: ${response.code}). لطفاً تنظیمات شبکه یا کلید API را بررسی کنید."
                }
                
                val responseBodyStr = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "جوابی از پایتون‌یار دریافت نشد.")
                        }
                    }
                }
                return@withContext "⚠️ پاسخ دریافتی خالی بود. لطفاً مجدداً تلاش کنید."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in askGemini: ${e.message}", e)
            return@withContext "🌍 خطا در برقراری ارتباط با سرویس ابری هوشمند. بررسی کنید که اینترنت متصل باشد.\nنوع خطا: ${e.localizedMessage ?: "نامشخص"}"
        }
    }
}
