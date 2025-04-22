package com.moodyday.app.network

import com.moodyday.app.BuildConfig
import com.moodyday.app.data.OpenAIRequest
import com.moodyday.app.data.OpenAIResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenAIApi {
    @Headers(
        "Content-Type: application/json"
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

object OpenAIService {
    private const val BASE_URL = "https://api.openai.com/"
    private const val API_KEY = BuildConfig.OPENAI_API_KEY

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenAIApi = retrofit.create(OpenAIApi::class.java)
} 