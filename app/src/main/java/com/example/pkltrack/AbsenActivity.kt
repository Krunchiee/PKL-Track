package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pkltrack.adapter.AttendanceAdapter
import com.example.pkltrack.model.Attendance
import com.example.pkltrack.model.AttendanceResponse
import com.example.pkltrack.model.AttendanceRequest
import com.example.pkltrack.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        recyclerHistory.layoutManager = LinearLayoutManager(this)

        //untuk membawa data ke header
        //untuk membawa data ke header
        val pref       = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama   = pref.getString("nama", "User")
        val nisn = pref.getString("nisn", "00000000")
        val kelas = pref.getString("kelas", "XI RPL 1")
        val foto = pref.getString("foto", "foto user")
        val idSiswa = pref.getInt("id_siswa", 0)

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisn+" - "+kelas
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        Glide.with(this).load(foto).into(profileImage)
        getAttendanceFromAPI(idSiswa)

        btnClockInActivity.setOnClickListener {
            startActivity(Intent(this, ClockInActivity::class.java))
        }
    }

    private fun getAttendanceFromAPI(idSiswa: Int) {

        val request = AttendanceRequest(idSiswa)
        ApiClient.getInstance(this).getAbsensi(request)
            .enqueue(object : Callback<AttendanceResponse> {
                override fun onResponse(call: Call<AttendanceResponse>, response: Response<AttendanceResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val attendanceData = response.body()?.data ?: emptyList()

                        val formattedList = attendanceData.map {
                            val isLate = it.keterangan.contains("terlambat", ignoreCase = true)
                            Attendance(
                                date = formatTanggalIndo(it.tanggal),
                                clockIn = it.jam_masuk.substring(11, 16),
                                clockOut = it.jam_keluar.substring(11, 16),
                                isLate = isLate
                            )
                        }

                        recyclerHistory.adapter = AttendanceAdapter(formattedList)
                    } else {
                        Toast.makeText(this@AbsenActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AttendanceResponse>, t: Throwable) {
                    Toast.makeText(this@AbsenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun formatTanggalIndo(tanggal: String): String {
        val localeID = Locale("in", "ID")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeID)

        return try {
            val date = inputFormat.parse(tanggal)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            tanggal // fallback kalau parsing gagal
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
