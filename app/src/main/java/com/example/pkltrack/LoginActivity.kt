package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pkltrack.network.ApiClient
import com.example.pkltrack.network.ApiService
import com.example.pkltrack.model.LoginResponse
import com.example.pkltrack.model.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class LoginActivity : AppCompatActivity() {

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val usernameEditText = findViewById<EditText>(R.id.editUsername)
//        val passwordEditText = findViewById<EditText>(R.id.editPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loginButton.setOnClickListener {
            val nisn = usernameEditText.text.toString().trim()

            if (nisn.isEmpty()) {
                toast("NISN harus diisi")
            } else {
                ApiClient.getInstance(this).loginSiswa(LoginRequest(nisn)).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginData = response.body()!!
                            val siswa = loginData.user.siswa

                            val pref = getSharedPreferences("UserData", MODE_PRIVATE)
                            with(pref.edit()) {
                                putString("token", loginData.token)
                                putString("nama", siswa.nama)
                                putString("nisn", siswa.nisn)
                                putString("kelas", siswa.kelas)
                                putString("foto", siswa.foto)
                                putInt("id_siswa", siswa.id)
                                apply()
                            }

                            toast("Login Berhasil")
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            toast("Login gagal: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        toast("Terjadi kesalahan: ${t.message}")
                    }
                })
            }
        }

    }
}
