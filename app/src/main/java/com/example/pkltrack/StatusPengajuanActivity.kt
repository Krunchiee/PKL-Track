package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pkltrack.model.*
import com.example.pkltrack.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Handler
import android.os.Looper

class StatusPengajuanActivity : AppCompatActivity() {

    private lateinit var txtNamaPerusahaan: TextView
    private lateinit var txtStatusPengajuan: TextView
    private lateinit var txtUser: TextView
    private lateinit var txtNISJurusan: TextView
    private lateinit var profileImage: ImageView

    private var siswaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_pengajuan)

        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        siswaId = pref.getInt("id_siswa", -1)

        if (siswaId == -1) {
            Toast.makeText(this, "Error: ID Siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val token = "Bearer " + (pref.getString("token", "") ?: "")

        txtUser = findViewById(R.id.txtUser)
        txtNISJurusan = findViewById(R.id.txtNISJurusan)
        profileImage = findViewById(R.id.profile_image)
        txtUser.text = nama
        txtNISJurusan.text = "$nisn - $kelas"

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        txtNamaPerusahaan = findViewById(R.id.txtNamaPerusahaan)
        txtStatusPengajuan = findViewById(R.id.txtStatusPengajuan)

        getStatusPengajuan(token)
    }

    private fun getStatusPengajuan(token: String) {
        ApiClient.getInstance(this).cekPengajuanSiswa(token, siswaId)
            .enqueue(object : Callback<PengajuanInfoResponse> {
                override fun onResponse(
                    call: Call<PengajuanInfoResponse>,
                    response: Response<PengajuanInfoResponse>
                ) {
                    if (response.isSuccessful && response.body()?.has_pengajuan == true) {
                        val data = response.body()!!.data!!

                        txtNamaPerusahaan.text = data.mitra.nama_mitra
                        txtStatusPengajuan.text = "Status: ${getStatusLabel(data.status)}"
                        txtUser.text = data.siswa.nama
                        txtNISJurusan.text = "${data.siswa.nisn} - ${data.siswa.kelas}"

                    } else {
                        txtStatusPengajuan.text = "Belum ada pengajuan."
                        txtNamaPerusahaan.text = "-"
                    }
                }

                override fun onFailure(call: Call<PengajuanInfoResponse>, t: Throwable) {
                    Toast.makeText(
                        this@StatusPengajuanActivity,
                        "Gagal memuat data: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getStatusLabel(status: String?): String {
        return when (status) {
            "0" -> "Dikirim"
            "1" -> "Diterima"
            "2" -> "Ditolak"
            else -> "Tidak diketahui"
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
