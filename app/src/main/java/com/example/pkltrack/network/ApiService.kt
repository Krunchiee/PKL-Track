package com.example.pkltrack.network

import com.example.pkltrack.model.LoginResponse
import com.example.pkltrack.model.LoginRequest
import com.example.pkltrack.model.AttendanceResponse
import com.example.pkltrack.model.AttendanceRequest
import com.example.pkltrack.model.ClockInRequest
import com.example.pkltrack.model.KoordinatResponse
import com.example.pkltrack.model.ClockInResponse
import com.example.pkltrack.model.DailyReportResponse
import com.example.pkltrack.model.PenilaianResponse
import com.example.pkltrack.model.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

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

    @GET("me")
    fun getProfile(@Header("Authorization") token: String): Call<ProfileResponse>

    @Multipart
    @POST("siswa/update/{id}")
    fun updateProfile(
        @Path("id") id: Int,
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part foto: MultipartBody.Part?
    ): Call<ResponseBody>


    @POST("logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<Void>

    @GET("siswa/penilaian/{id}")
    fun getPenilaian(@Path("id") id: Int): Call<PenilaianResponse>

    @Multipart
    @POST("siswa/laporan")
    fun uploadLaporan(
        @Part("id_siswa") idSiswa: Int,
        @Part("keterangan") keterangan: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<DailyReportResponse>
}

