package com.example.pkltrack.data

import com.example.pkltrack.model.User

class DummyUserDataSource : UserDataSource {

    /** daftar user dummy */
    private val users = listOf(
        User("admin",  "1234", "27738749", "Teknik Ringan"),
        User("budi",   "0000", "27738888", "RPL"),
        User("santi",  "1111", "27739999", "TKJ")
    )

    override fun login(username: String, password: String): User? =
        users.firstOrNull { it.username == username && it.password == password }
}