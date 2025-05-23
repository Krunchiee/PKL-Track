package com.example.pkltrack.repository

import com.example.pkltrack.data.DummyUserDataSource       // ganti setelah ada API
import com.example.pkltrack.data.UserDataSource
import com.example.pkltrack.model.User

class UserRepository(private val dataSource: UserDataSource = DummyUserDataSource()) {

    fun login(username: String, password: String): User? =
        dataSource.login(username, password)
}