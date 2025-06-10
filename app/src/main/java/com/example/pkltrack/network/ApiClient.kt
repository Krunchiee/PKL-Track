package com.example.pkltrack.network

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://pkltrack.my.id/api/"

    fun getInstance(context: Context): ApiService {
        val pref = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val token = pref.getString("token", null)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder().apply {
            addInterceptor(logging) // << Tambahkan ini
            token?.let {
                addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $it")
                        .addHeader("Accept", "application/json")
                        .build()
                    chain.proceed(newRequest)
                }
            }
        }.build()

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ApiService::class.java)
    }

}