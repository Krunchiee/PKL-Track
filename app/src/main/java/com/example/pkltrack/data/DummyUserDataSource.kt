package com.example.pkltrack.data

import com.example.pkltrack.model.User

class DummyUserDataSource : UserDataSource {

    /** daftar user dummy */
    private val users = listOf(
        User("4444",  "Admin", "27738749", "Teknik Baja Ringan"),
        User("0000",   "Budi", "27738888", "Teknik Kerangka Jembatan"),
        User("1111",  "Siti", "27739999", "Teknik Mesin Pesawat")
    )

//    override fun login(username: String, password: String): User? =
//        users.firstOrNull { it.username == username && it.password == password }

    override fun login(nisn: String): User? =
        users.firstOrNull { it.nisn == nisn }
}