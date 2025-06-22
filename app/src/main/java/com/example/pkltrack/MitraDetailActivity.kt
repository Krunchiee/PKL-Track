package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pkltrack.PengajuanActivity

class MitraDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mitra_detail)

        val nama = intent.getStringExtra("nama")
        val alamat = intent.getStringExtra("alamat")
        val lowongan = intent.getStringExtra("lowongan")
        val keterangan = intent.getStringExtra("keterangan")

        val txtNamaMitra = findViewById<TextView>(R.id.txtNamaMitra)
        val txtDetail = findViewById<TextView>(R.id.txtDetail)
        val idMitra = intent.getIntExtra("id_mitra", 0)

        txtNamaMitra.text = nama
        txtDetail.text = """
            • Lokasi: $alamat
            • Jumlah Lowongan: $lowongan
            • Kriteria: $keterangan
            • Persyaratan: -
        """.trimIndent()

        val btnAjukan = findViewById<Button>(R.id.btnAjukan)
        btnAjukan.setOnClickListener {
            val intent = Intent(this, PengajuanActivity::class.java)
            intent.putExtra("id_mitra", idMitra)
            startActivity(intent)
        }
    }
}