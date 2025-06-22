package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pkltrack.adapter.MitraAdapter
import com.example.pkltrack.model.MitraResponse
import com.example.pkltrack.model.PengajuanInfoResponse
import com.example.pkltrack.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AvailableMitraActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mitraAdapter: MitraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_available_mitra)

        recyclerView = findViewById(R.id.recyclerMitra)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        val idSiswa = pref.getInt("id_siswa", -1)
        val token = "Bearer " + (pref.getString("token", "") ?: "")

        findViewById<TextView>(R.id.txtUser).text = nama
        findViewById<TextView>(R.id.txtNISJurusan).text = "$nisn - $kelas"
        val profileImage = findViewById<ImageView>(R.id.profile_image)

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).into(profileImage)
        }

        if (idSiswa != -1) {
            cekPengajuan(token, idSiswa)
        } else {
            Toast.makeText(this, "ID siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cekPengajuan(token: String, idSiswa: Int) {
        ApiClient.getInstance(this).cekPengajuanSiswa(token, idSiswa)
            .enqueue(object : Callback<PengajuanInfoResponse> {
                override fun onResponse(call: Call<PengajuanInfoResponse>, response: Response<PengajuanInfoResponse>) {
                    if (response.isSuccessful && response.body()?.has_pengajuan == true) {
                        // Sudah punya pengajuan, arahkan ke StatusPengajuanActivity
                        val intent = Intent(this@AvailableMitraActivity, StatusPengajuanActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: agar tidak bisa kembali ke halaman ini
                    } else {
                        // Tidak ada pengajuan, baru tampilkan mitra
                        tampilkanListMitra()
                    }
                }

                override fun onFailure(call: Call<PengajuanInfoResponse>, t: Throwable) {
                    Toast.makeText(this@AvailableMitraActivity, "Gagal mengecek pengajuan: ${t.message}", Toast.LENGTH_SHORT).show()
                    tampilkanListMitra() // fallback: tetap tampilkan mitra
                }
            })
    }

    private fun tampilkanListMitra() {
        ApiClient.getInstance(this).getAvailableMitra()
            .enqueue(object : Callback<MitraResponse> {
                override fun onResponse(call: Call<MitraResponse>, response: Response<MitraResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val mitraList = response.body()?.data ?: emptyList()

                        mitraAdapter = MitraAdapter(mitraList) { mitra ->
                            val intent = Intent(this@AvailableMitraActivity, MitraDetailActivity::class.java)
                            intent.putExtra("id_mitra", mitra.id ?: "")
                            intent.putExtra("nama", mitra.nama_mitra ?: "")
                            intent.putExtra("alamat", mitra.alamat ?: "")
                            intent.putExtra("lowongan", mitra.jmlh_lowongan ?: "")
                            intent.putExtra("keterangan", mitra.keterangan ?: "")
                            startActivity(intent)
                        }
                        recyclerView.adapter = mitraAdapter
                    } else {
                        Toast.makeText(this@AvailableMitraActivity, "Gagal memuat data mitra", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MitraResponse>, t: Throwable) {
                    Toast.makeText(this@AvailableMitraActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}