package com.example.pkltrack.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Data classes
data class ClockInRequest(
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val time: String,
    val note: String?
)

data class ClockInResponse(
    val success: Boolean,
    val message: String
)

// Retrofit service
interface ApiService {
    @POST("clockin")
    suspend fun postClockIn(@Body body: ClockInRequest): ClockInResponse
}

object ApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/") // ganti base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}