package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pkltrack.repository.UserRepository

class LoginActivity : AppCompatActivity() {

    private val userRepo = UserRepository()

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val usernameEditText = findViewById<EditText>(R.id.editUsername)
        val passwordEditText = findViewById<EditText>(R.id.editPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                val user = userRepo.login(username, password)
                if (user != null) {
                    // --- simpan di SharedPreferences ---
                    val pref = getSharedPreferences("UserData", MODE_PRIVATE)
                    with(pref.edit()) {
                        putString("username", user.username)
                        putString("nisJurusan", "${user.nis} - ${user.jurusan}")
                        apply()
                    }

                    toast("Login Berhasil")
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    toast("Username atau Password salah")
                }
            }
        }

    }
}
