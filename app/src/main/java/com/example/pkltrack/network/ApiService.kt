package com.example.pkltrack.network

import com.example.pkltrack.model.LoginResponse
import com.example.pkltrack.model.LoginRequest
import com.example.pkltrack.model.AttendanceResponse
import com.example.pkltrack.model.AttendanceRequest
import com.example.pkltrack.model.ClockInRequest
import com.example.pkltrack.model.KoordinatResponse
import com.example.pkltrack.model.ClockInResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login-siswa")
    fun loginSiswa(@Body request: LoginRequest): Call<LoginResponse>

    @POST("absen/history")
    fun getAbsensi(@Body request: AttendanceRequest): Call<AttendanceResponse>
    @POST("absen/koordinat")
    fun getKoordinat(@Body request: Map<String, Int>): Call<KoordinatResponse>
    @POST("absen/store")
    fun postClockIn(
        @Body body: ClockInRequest
    ): Call<ClockInResponse>
}

