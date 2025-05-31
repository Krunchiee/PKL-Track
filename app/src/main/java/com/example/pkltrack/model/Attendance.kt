package com.example.pkltrack.model

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