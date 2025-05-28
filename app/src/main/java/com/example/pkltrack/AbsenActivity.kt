package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pkltrack.adapter.AttendanceAdapter
import com.example.pkltrack.model.Attendance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AbsenActivity : AppCompatActivity() {

    private lateinit var txtLiveTime: TextView
    private lateinit var txtLiveDate: TextView
    private lateinit var recyclerHistory: RecyclerView
    private lateinit var btnClockInActivity: Button
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)

        txtLiveTime = findViewById(R.id.txtLiveTime)
        txtLiveDate = findViewById(R.id.txtLiveDate)
        recyclerHistory = findViewById(R.id.recyclerHistory)
        btnClockInActivity= findViewById(R.id.btnClockIn)

        // Tampilkan waktu dan tanggal sekarang
        updateTime()

        // Data dummy untuk riwayat absensi
        val dummyHistory = listOf(
            Attendance("Senin, 20 Mei 2024", "08:01", "16:00", true),
            Attendance("Selasa, 21 Mei 2024", "07:55", "16:05", false),
            Attendance("Rabu, 22 Mei 2024", "08:00", "16:00", false)
        )

        recyclerHistory.layoutManager = LinearLayoutManager(this)
        recyclerHistory.adapter = AttendanceAdapter(dummyHistory)

        //untuk membawa data ke header
        val pref       = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama   = pref.getString("nama", "User")
        val nisJurusan = pref.getString("nisJurusan", "00000000 - Jurusan")

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisJurusan

        btnClockInActivity.setOnClickListener {
            startActivity(Intent(this, ClockInActivity::class.java))
        }
    }

    private fun updateTime() {
        handler.post(object : Runnable {
            override fun run() {
                val localeID = Locale("in", "ID")

                val currentTime = SimpleDateFormat("HH.mm", localeID).format(Date())
                val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", localeID).format(Date())

                txtLiveTime.text = currentTime
                txtLiveDate.text = currentDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeID) else it.toString() }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
