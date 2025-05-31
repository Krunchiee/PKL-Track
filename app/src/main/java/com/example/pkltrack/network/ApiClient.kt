package com.example.pkltrack.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://pkltrack.my.id/api/"

    fun getInstance(context: Context): ApiService {
        val pref = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = pref.getString("token", null)

        val client = OkHttpClient.Builder().apply {
            token?.let {
                addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $it")
                        .build()
                    chain.proceed(newRequest)
                }
            }
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}

