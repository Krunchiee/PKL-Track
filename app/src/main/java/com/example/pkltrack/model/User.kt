package com.example.pkltrack.model

data class User(
    val username: String,
    val password: String,      // hash kalau sudah production
    val nis: String,
    val jurusan: String
)