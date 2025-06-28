package com.example.pkltrack

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pkltrack.adapter.NotificationAdapter
import com.example.pkltrack.model.NotificationResponse
import com.example.pkltrack.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView
import com.bumptech.glide.Glide


class NotificationActivity : AppCompatActivity() {

    private fun updateNotifikasi(nisn: String) {
        ApiClient.getInstance(this).updateNotifikasi(nisn)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("NOTIF", "Status notifikasi berhasil diupdate")
                    } else {
                        Log.w("NOTIF", "Gagal update notifikasi: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("NOTIF", "Error update notifikasi", t)
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tandai bahwa halaman notifikasi sudah dibuka
        getSharedPreferences("Notif", MODE_PRIVATE)
            .edit()
            .putBoolean("opened", true)
            .apply()

        setContentView(R.layout.activity_notification)
        // 🔽 Tambahan di sini
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "") ?: ""

        val headerView = findViewById<View>(R.id.include_header)
        val txtUser = headerView.findViewById<TextView>(R.id.txtUser)
        val txtNISJurusan = headerView.findViewById<TextView>(R.id.txtNISJurusan)
        val imgProfile = headerView.findViewById<ImageView>(R.id.profile_image)

        txtUser.text = nama
        txtNISJurusan.text = "$nisn - $kelas"

        if (foto.isNotEmpty()) {
            Glide.with(this)
                .load(foto)
                .placeholder(R.drawable.ic_user_24)
                .circleCrop()
                .into(imgProfile)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ApiClient.getInstance(this).getNotifications(nisn)
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val notifications = response.body()?.data ?: emptyList()
                        recyclerView.adapter = NotificationAdapter(notifications)

                        val baru = notifications.count { it.isNew }

                        val badge = findViewById<TextView>(R.id.badgeCount)
                        if (badge != null) {
                            if (baru > 0) {
                                badge.visibility = View.VISIBLE
                                badge.text = baru.toString()
                            } else {
                                badge.visibility = View.GONE
                            }
                        } else {
                            Log.e("NOTIF", "badgeCount TextView tidak ditemukan di layout!")
                        }
                        if (notifications.any { it.isNew }) {
                            updateNotifikasi(nisn)
                        }

                    } else {
                        Toast.makeText(
                            this@NotificationActivity,
                            "Tidak ada notifikasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    Toast.makeText(
                        this@NotificationActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("NOTIF", "Network failure", t)
                }
            })
    }
}
