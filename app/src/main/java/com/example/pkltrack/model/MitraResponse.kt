package com.example.pkltrack.model

data class MitraResponse(
    val success: Boolean,
    val message: String,
    val data: List<Mitra>
)

data class Mitra(
    val id: Int,
    val nama_mitra: String,
    val alamat: String,
    val jmlh_lowongan: String,
    val kontak_mitra: String,
    val keterangan: String
)
