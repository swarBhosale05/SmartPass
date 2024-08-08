package com.ebuspass.smartpassapp
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class ChatbotManager(private val context: Context) {
    private val apiKey = "eb7049bad0msh61d042161ab19fep1f5c3fjsn73bc4b3c1134"
    private val host = "chatgpt-42.p.rapidapi.com"

    fun sendMessage(message: String, onResponse: (String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val mediaType = MediaType.parse("application/json")
            val requestBody = """
                {
                    "messages": [
                        {
                            "role": "user",
                            "content": "$message"
                        }
                    ],
                    "system_prompt": "",
                    "temperature": 0.9,
                    "top_k": 5,
                    "top_p": 0.9,
                    "max_tokens": 256,
                    "web_access": false
                }
            """.trimIndent()
            val request = Request.Builder()
                .url("https://chatgpt-42.p.rapidapi.com/conversationgpt4-2")
                .post(RequestBody.create(mediaType, requestBody))
                .addHeader("Content-Type", "application/json")
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", host)
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body()?.string()
                val parsedResponse = parseResponse(responseBody)
                withContext(Dispatchers.Main) {
                    onResponse(parsedResponse)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onResponse(null)
                }
            }
        }
    }

    private fun parseResponse(response: String?): String? {
        return try {
            val jsonObject = JSONObject(response)
            val choicesArray = jsonObject.optJSONArray("choices")
            val messageObject = choicesArray?.optJSONObject(0)?.optJSONObject("message")
            messageObject?.optString("content")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}