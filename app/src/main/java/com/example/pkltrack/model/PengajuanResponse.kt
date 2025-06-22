package com.example.pkltrack.model

data class PengajuanResponse(
    val success: Boolean,
    val message: String,
    val data: PengajuanData?
)

data class PengajuanData(
    val id: Int,
    val id_siswa: Int,
    val id_mitra: Int,
    val berkas_cv: String,
    val status: String,
    val created_at: String,
    val updated_at: String
)

data class PengajuanInfoResponse(
    val success: Boolean,
    val has_pengajuan: Boolean,
    val status: String?,
    val data: PengajuanInfoData?
)

data class PengajuanInfoData(
    val id: Int,
    val status: String,
    val tgl_mulai: String?,
    val tgl_selesai: String?,
    val mitra: PengajuanInfoMitra,
    val siswa: PengajuanInfoSiswa
)

data class PengajuanInfoMitra(
    val nama_mitra: String,
    val alamat: String,
    val kontak_mitra: String
)

data class PengajuanInfoSiswa(
    val nama: String,
    val kelas: String,
    val foto: String,
    val nisn: String
)