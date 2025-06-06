package com.example.pkltrack.model

import com.google.gson.annotations.SerializedName

data class Attendance(
    val date: String,
    val clockIn: String,
    val clockOut: String,
    val isLate: Boolean
)

data class AttendanceRequest(val id_siswa: Int)

data class AttendanceResponse(
    val success: Boolean,
    val message: String,
    val data: List<AttendanceItem>
)

data class AttendanceItem(
    val id: Int,
    val id_siswa: Int,
    val tanggal: String,
    val jam_masuk: String,
    val jam_keluar: String,
    val status: String,
    val keterangan: String
)


data class ClockInRequest(
    @SerializedName("id_siswa") val idSiswa: Int,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("status") val status: String,
    @SerializedName("keterangan") val keterangan: String
)


data class ClockInResponse(
    val success: Boolean,
    val message: String
)