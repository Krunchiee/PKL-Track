package com.example.pkltrack.model

data class LoginResponse(
    val token: String,
    val user: User
)

data class LoginRequest(
    val nisn: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String,
    val siswa: Siswa
)

data class Siswa(
    val id: Int,
    val id_akun: String,
    val nisn: String,
    val nama: String,
    val kelas: String,
    val id_jurusan: String,
    val no_hp: String,
    val alamat: String,
    val foto: String
)
