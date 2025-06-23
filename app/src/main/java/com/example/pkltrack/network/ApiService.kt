package com.example.pkltrack.network

import com.example.pkltrack.model.LoginResponse
import com.example.pkltrack.model.LoginRequest
import com.example.pkltrack.model.AttendanceResponse
import com.example.pkltrack.model.AttendanceRequest
import com.example.pkltrack.model.ClockInRequest
import com.example.pkltrack.model.KoordinatResponse
import com.example.pkltrack.model.ClockInResponse
import com.example.pkltrack.model.DailyReportResponse
import com.example.pkltrack.model.MitraResponse
import com.example.pkltrack.model.JurusanListResponse
import com.example.pkltrack.model.PengajuanResponse
import com.example.pkltrack.model.PengajuanInfoResponse
import com.example.pkltrack.model.PenilaianResponse
import com.example.pkltrack.model.ProfileResponse
import com.example.pkltrack.model.ServerTimeResponse
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
        @Part photo: MultipartBody.Part?
    ): Call<ResponseBody>

    @GET("siswa/mitra/available")
    fun getAvailableMitra(): Call<MitraResponse>

    @Multipart
    @POST("siswa/pengajuan-awal")
    fun submitPengajuan(
        @Header("Authorization") token: String,
        @Part("id_siswa") idSiswa: RequestBody,
        @Part("id_mitra") idMitra: RequestBody,
        @Part berkasCV: MultipartBody.Part
    ): Call<PengajuanResponse>

    @GET("siswa/list-jurusan")
    fun getListJurusan(
        @Header("Authorization") token: String
    ): Call<JurusanListResponse>

    @GET("siswa/cek-pengajuan/{id}")
    fun cekPengajuanSiswa(
        @Header("Authorization") token: String,
        @Path("id") idSiswa: Int
    ): Call<PengajuanInfoResponse>

    @GET("absen/get-server-time")
    fun getServerTime(): Call<ServerTimeResponse>

}

