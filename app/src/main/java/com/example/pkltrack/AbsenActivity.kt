package com.example.pkltrack

import android.content.Intent
import android.graphics.Color
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
import com.example.pkltrack.model.ServerTimeResponse
import com.example.pkltrack.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AbsenActivity : AppCompatActivity() {

    private lateinit var txtLiveTime: TextView
    private lateinit var txtLiveDate: TextView
    private lateinit var recyclerHistory: RecyclerView
    private lateinit var btnClockInActivity: Button
    private lateinit var btnClockOutActivity: Button
    private val handler = Handler()
    private var serverTimeInMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)

        txtLiveTime = findViewById(R.id.txtLiveTime)
        txtLiveDate = findViewById(R.id.txtLiveDate)
        recyclerHistory = findViewById(R.id.recyclerHistory)
        btnClockInActivity= findViewById(R.id.btnClockIn)
        btnClockOutActivity= findViewById(R.id.btnClockOut)

        // Tampilkan waktu dan tanggal sekarang
        updateTimeFromServer()

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
        Glide.with(this).load(foto).circleCrop().into(profileImage)
        getServerTimeAndAttendance(idSiswa)

        btnClockInActivity.setOnClickListener {
            startActivity(Intent(this, ClockInActivity::class.java))
        }

        btnClockOutActivity.setOnClickListener {
            startActivity(Intent(this, ClockOutActivity::class.java))
        }
    }

//    private fun getAttendanceFromAPI(idSiswa: Int) {
//
//        val request = AttendanceRequest(idSiswa)
//        ApiClient.getInstance(this).getAbsensi(request)
//            .enqueue(object : Callback<AttendanceResponse> {
//                override fun onResponse(call: Call<AttendanceResponse>, response: Response<AttendanceResponse>) {
//                    if (response.isSuccessful && response.body()?.success == true) {
//                        val attendanceData = response.body()?.data ?: emptyList()
//
//                        val formattedList = attendanceData.map {
//                            val isLate = it.status?.contains("telat", ignoreCase = true) == true
//                            val clockIn = it.jam_masuk?.substring(11, 16) ?: ""
//                            val clockOut = it.jam_keluar?.substring(11, 16) ?: ""
//
//                            Attendance(
//                                date = formatTanggalIndo(it.tanggal),
//                                clockIn = clockIn,
//                                clockOut = clockOut,
//                                isLate = isLate
//                            )
//                        }
//
//                        recyclerHistory.adapter = AttendanceAdapter(formattedList)
//                    } else {
//                        Toast.makeText(this@AbsenActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<AttendanceResponse>, t: Throwable) {
//                    Toast.makeText(this@AbsenActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    private fun getAttendanceFromAPI(idSiswa: Int, hour: Int, minute: Int, todayStr: String) {
        val request = AttendanceRequest(idSiswa)
        ApiClient.getInstance(this).getAbsensi(request)
            .enqueue(object : Callback<AttendanceResponse> {
                override fun onResponse(call: Call<AttendanceResponse>, response: Response<AttendanceResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val attendanceData = response.body()?.data ?: emptyList()

                        // 🔹 Aturan 1: Berdasarkan waktu server
                        if (hour > 7 || (hour == 7 && minute > 15)) {
                            disableButton(btnClockInActivity)
                        }
                        if (hour < 17) {
                            disableButton(btnClockOutActivity)
                        }

                        // 🔹 Aturan 2: Jika absensi hari ini sudah ada
                        val todayAttendance = attendanceData.firstOrNull { it.tanggal == todayStr }

                        todayAttendance?.let {
                            if (!it.jam_masuk.isNullOrBlank()) {
                                disableButton(btnClockInActivity)
                            }
                            if (!it.jam_keluar.isNullOrBlank()) {
                                disableButton(btnClockOutActivity)
                            }
                        }

                        // 🔹 Set adapter recyclerView
                        val formattedList = attendanceData.map {
                            val isLate = it.status?.contains("telat", ignoreCase = true) == true
                            val clockIn = it.jam_masuk?.substring(11, 16) ?: ""
                            val clockOut = it.jam_keluar?.substring(11, 16) ?: ""

                            Attendance(
                                date = formatTanggalIndo(it.tanggal),
                                clockIn = clockIn,
                                clockOut = clockOut,
                                isLate = isLate
                            )
                        }

                        recyclerHistory.adapter = AttendanceAdapter(formattedList)
                    } else {
                        Toast.makeText(this@AbsenActivity, "Gagal mengambil data absensi", Toast.LENGTH_SHORT).show()
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

    private fun updateTimeFromServer() {
        ApiClient.getInstance(this).getServerTime()
            .enqueue(object : Callback<ServerTimeResponse> {
                override fun onResponse(call: Call<ServerTimeResponse>, response: Response<ServerTimeResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverTimeStr = response.body()!!.data.server_time // format: "2025-06-23 14:28:26"
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val parsedDate = sdf.parse(serverTimeStr)

                        if (parsedDate != null) {
                            serverTimeInMillis = parsedDate.time
                            startLiveClock() // mulai update jam berdasarkan millis
                        }
                    } else {
                        Toast.makeText(this@AbsenActivity, "Gagal ambil waktu server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ServerTimeResponse>, t: Throwable) {
                    Toast.makeText(this@AbsenActivity, "Gagal koneksi ke waktu server", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startLiveClock() {
        handler.post(object : Runnable {
            override fun run() {
                if (serverTimeInMillis > 0) {
                    val currentTime = Date(serverTimeInMillis)
                    val localeID = Locale("in", "ID")

                    val timeFormat = SimpleDateFormat("HH.mm", localeID)
                    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeID)

                    txtLiveTime.text = timeFormat.format(currentTime)
                    txtLiveDate.text = dateFormat.format(currentTime)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeID) else it.toString() }

                    serverTimeInMillis += 1000 // tambahkan 1 detik
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val idSiswa = pref.getInt("id_siswa", 0)

        getServerTimeAndAttendance(idSiswa)
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        button.alpha = 0.5f
        button.setBackgroundColor(Color.parseColor("#BDBDBD"))
    }

    private fun getServerTimeAndAttendance(idSiswa: Int) {
        ApiClient.getInstance(this).getServerTime()
            .enqueue(object : Callback<ServerTimeResponse> {
                override fun onResponse(call: Call<ServerTimeResponse>, response: Response<ServerTimeResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverTimeStr = response.body()!!.data.server_time // "2025-06-23 14:28:26"
                        val serverDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(serverTimeStr)

                        serverDate?.let {
                            val serverCal = Calendar.getInstance()
                            serverCal.time = it

                            val hour = serverCal.get(Calendar.HOUR_OF_DAY)
                            val minute = serverCal.get(Calendar.MINUTE)
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)

                            // 🔹 Lanjut ambil data absensi
                            getAttendanceFromAPI(idSiswa, hour, minute, todayStr)
                        }
                    } else {
                        Toast.makeText(this@AbsenActivity, "Gagal ambil waktu server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ServerTimeResponse>, t: Throwable) {
                    Toast.makeText(this@AbsenActivity, "Gagal koneksi ke server time", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
