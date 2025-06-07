package com.example.pkltrack.model


data class ProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String,
    val siswa: Profil
)

data class Profil(
    val id: Int,
    val id_akun: String,
    val nisn: String,
    val nama: String,
    val kelas: String,
    val id_jurusan: String,
    val no_hp: String,
    val alamat: String,
    val foto: String,
    val id_tahun_ajaran: String
)
