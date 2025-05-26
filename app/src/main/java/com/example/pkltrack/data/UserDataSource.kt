package com.example.pkltrack.data

import com.example.pkltrack.model.User

interface UserDataSource {
    /** @return user jika username & password valid, null kalau gagal */
//    fun login(username: String, password: String): User?
    fun login(username: String): User?
}