package com.example.pkltrack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        txtNamaMitra.text = nama
        txtDetail.text = """
            • Lokasi: $alamat
            • Jumlah Lowongan: $lowongan
            • Kriteria: $keterangan
            • Persyaratan: -
        """.trimIndent()

        val btnAjukan = findViewById<Button>(R.id.btnAjukan)
        btnAjukan.setOnClickListener {
            Toast.makeText(this, "Pengajuan dikirim", Toast.LENGTH_SHORT).show()
        }
    }
}