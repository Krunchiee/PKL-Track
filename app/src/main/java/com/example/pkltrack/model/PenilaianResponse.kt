package com.example.pkltrack.model

data class PenilaianResponse(
    val success: Boolean,
    val message: String,
    val data: PenilaianData?
)

data class PenilaianData(
    val id: Int,
    val id_siswa: Int,
    val nilai: Int,
    val catatan: String?,
    val created_at: String?,
    val updated_at: String?,
    val sertifikat: String?
)
