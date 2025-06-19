package com.hfad.teachershelper.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://127.0.0.1:8000"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)   // Время подключения
        .readTimeout(60, TimeUnit.SECONDS)      // Время чтения
        .writeTimeout(60, TimeUnit.SECONDS)     // Время записи
        .build()

    val apiService: MainAPI = Retrofit.Builder()
        .baseUrl(MainAPI.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MainAPI::class.java)
}