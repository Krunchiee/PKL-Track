package com.example.pkltrack.model

data class PenilaianResponse(
    val success: Boolean,
    val message: String,
    val data: PenilaianWrapper?
)

data class PenilaianWrapper(
    val pengajuan: PenilaianData?,
    val sudah_dinilai: Boolean
)

data class PenilaianData(
    val id: Int,
    val id_siswa: String,
    val nilai_kompetensi: String?,
    val catatan: String?,
    val created_at: String?,
    val updated_at: String?,
    val sertifikat: String?
)
