package com.example.pkltrack.model


import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("siswa") val siswa: Profile?
)


data class Profile(
    val id: Int,
    val id_akun: String,
    val nisn: String,
    val nama: String,
    val kelas: String,
    val id_jurusan: String,
    val no_hp: String,
    val alamat: String,
    val foto: String?,
    val id_tahun_ajaran: String
)


data class Akun(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)
