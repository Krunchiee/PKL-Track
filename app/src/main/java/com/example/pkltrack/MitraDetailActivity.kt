package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.pkltrack.PengajuanActivity

class MitraDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_mitra_detail)

        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisn+" - "+kelas
        val profileImage = findViewById<ImageView>(R.id.profile_image)

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        val namaMitra = intent.getStringExtra("nama")
        val alamat = intent.getStringExtra("alamat")
        val lowongan = intent.getStringExtra("lowongan")
        val keterangan = intent.getStringExtra("keterangan")

        val txtNamaMitra = findViewById<TextView>(R.id.txtNamaMitra)
        val txtLokasi = findViewById<TextView>(R.id.txtLokasi)
        val txtJumlahLowongan = findViewById<TextView>(R.id.txtJumlahLowongan)
        val txtKriteria = findViewById<TextView>(R.id.txtKriteria)
        val idMitra = intent.getIntExtra("id_mitra", 0)

        txtNamaMitra.text = namaMitra ?: "Nama tidak tersedia"
        txtLokasi.text = "Alamat: ${alamat ?: "Tidak tersedia"}"
        txtJumlahLowongan.text = "Jumlah Lowongan: ${lowongan ?: "Tidak tersedia"}"
        txtKriteria.text = "Kriteria: ${keterangan ?: "Tidak tersedia"}"

        val btnAjukan = findViewById<Button>(R.id.btnAjukan)
        btnAjukan.setOnClickListener {
            val intent = Intent(this, PengajuanActivity::class.java)
            intent.putExtra("id_mitra", idMitra)
            startActivity(intent)
        }
    }
}