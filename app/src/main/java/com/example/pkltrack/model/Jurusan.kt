package com.example.pkltrack.model

data class Jurusan(
    val id: Int,
    val nama_jurusan: String
)

data class JurusanListResponse(
    val success: Boolean,
    val data: List<Jurusan>
)
