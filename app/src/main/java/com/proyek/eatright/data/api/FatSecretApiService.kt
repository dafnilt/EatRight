package com.proyek.eatright.data.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.proyek.eatright.data.model.Food
import com.proyek.eatright.data.model.FoodSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.Base64
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class FatSecretApiService {
    private val baseUrl = "https://platform.fatsecret.com/rest/server.api"
    private val consumerKey = "37c9ffa61d374ec385962271560c7428"
    private val consumerSecret = "92a3c38b4ec14bc9b7dba6cca5281dcc"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun searchFoods(query: String): FoodSearchResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Current timestamp
                val timestamp = (System.currentTimeMillis() / 1000).toString()

                // Random nonce
                val nonce = UUID.randomUUID().toString()

                // Base parameters
                val params = mapOf(
                    "method" to "foods.search",
                    "search_expression" to query,
                    "format" to "json",
                    "oauth_consumer_key" to consumerKey,
                    "oauth_signature_method" to "HMAC-SHA1",
                    "oauth_timestamp" to timestamp,
                    "oauth_nonce" to nonce,
                    "oauth_version" to "1.0"
                )

                // Create signature base string
                val baseString = createSignatureBaseString("GET", baseUrl, params)

                // Generate signature
                val signature = generateSignature(baseString, consumerSecret)

                // Build URL with parameters
                val urlBuilder = baseUrl.toHttpUrlOrNull()!!.newBuilder()
                params.forEach { (key, value) ->
                    urlBuilder.addQueryParameter(key, value)
                }
                urlBuilder.addQueryParameter("oauth_signature", signature)

                val request = Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        Log.e("FatSecretApiService", "Request failed with code: ${response.code}, Body: $errorBody")
                        return@withContext FoodSearchResponse(emptyList())
                    }

                    val responseData = response.body?.string() ?: throw IOException("Empty response body")
                    Log.d("FatSecretApiService", "Raw response: $responseData")

                    parseSearchResponse(responseData)
                }
            } catch (e: Exception) {
                Log.e("FatSecretApiService", "Network error", e)
                FoodSearchResponse(emptyList())
            }
        }
    }

    private fun createSignatureBaseString(method: String, url: String, params: Map<String, String>): String {
        val sortedParams = params.toSortedMap()
        val paramString = sortedParams.map { (key, value) ->
            "${urlEncode(key)}=${urlEncode(value)}"
        }.joinToString("&")

        return method + "&" + urlEncode(url) + "&" + urlEncode(paramString)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSignature(baseString: String, secret: String): String {
        val keyString = "$secret&"
        val keySpec = SecretKeySpec(keyString.toByteArray(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)

        val signature = mac.doFinal(baseString.toByteArray())
        return Base64.getEncoder().encodeToString(signature)
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }

    private fun parseSearchResponse(jsonString: String): FoodSearchResponse {
        try {
            val jsonObject = JSONObject(jsonString)

            // Handle error response
            if (jsonObject.has("error")) {
                val error = jsonObject.getJSONObject("error")
                val code = error.optInt("code")
                val message = error.optString("message")
                Log.e("FatSecretApiService", "API Error: Code $code, Message: $message")
                return FoodSearchResponse(emptyList())
            }

            // Handle successful response
            if (jsonObject.has("foods")) {
                val foodsObject = jsonObject.getJSONObject("foods")
                val foodList = mutableListOf<Food>()

                if (foodsObject.has("food")) {
                    val foodArray = when {
                        foodsObject.get("food") is JSONArray -> foodsObject.getJSONArray("food")
                        else -> {
                            val array = JSONArray()
                            array.put(foodsObject.getJSONObject("food"))
                            array
                        }
                    }

                    for (i in 0 until foodArray.length()) {
                        try {
                            val foodObject = foodArray.getJSONObject(i)

                            val food = Food(
                                id = foodObject.optString("food_id", ""),
                                name = foodObject.optString("food_name", ""),
                                description = foodObject.optString("food_description", ""),
                                type = foodObject.optString("food_type", ""),
                                url = foodObject.optString("food_url", "")
                            )

                            foodList.add(food)
                        } catch (e: Exception) {
                            Log.e("FatSecretApiService", "Error parsing food item", e)
                        }
                    }
                }

                return FoodSearchResponse(foodList)
            }

            return FoodSearchResponse(emptyList())
        } catch (e: Exception) {
            Log.e("FatSecretApiService", "Error parsing response", e)
            return FoodSearchResponse(emptyList())
        }
    }
}