package com.example.pkltrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.pkltrack.model.NotificationResponse
import com.example.pkltrack.model.PengajuanInfoResponse
import com.example.pkltrack.network.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log


class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: WormDotsIndicator
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var pendaftaranpkl: LinearLayout
    private lateinit var absensi: LinearLayout
    private lateinit var laporanHarian: LinearLayout
    private lateinit var penilaianMitra: LinearLayout
    private val slideInterval = 10000L // 3 seconds
    private var siswaId: Int = -1

    private val images = listOf(
        R.drawable.banner3,
        R.drawable.banner4,
        R.drawable.banner5
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.bannerSlider)
        viewPager.adapter = BannerAdapter(images)

        dotsIndicator = findViewById(R.id.dotsIndicator)
        dotsIndicator.setViewPager2(viewPager)

        handler = Handler(mainLooper)
        runnable = object : Runnable {
            override fun run() {
                val next = (viewPager.currentItem + 1) % images.size
                viewPager.setCurrentItem(next, true)
                handler.postDelayed(this, slideInterval)
            }
        }

        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }

        //untuk membawa data ke header
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        siswaId = pref.getInt("id_siswa", -1)

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisn+" - "+kelas
        val profileImage = findViewById<ImageView>(R.id.profile_image)

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        pendaftaranpkl = findViewById(R.id.pendaftaran_pkl)
        pendaftaranpkl.setOnClickListener {
            startActivity(Intent(this, AvailableMitraActivity::class.java))
        }

        absensi = findViewById(R.id.absensi)
        absensi.setOnClickListener {
            startActivity(Intent(this, AbsenActivity::class.java))
        }

        laporanHarian = findViewById(R.id.laporan_harian)
        laporanHarian.setOnClickListener {
            startActivity(Intent(this, DailyReportActivity::class.java))
        }

        penilaianMitra = findViewById(R.id.penilaian_mitra)
        penilaianMitra.setOnClickListener {
            startActivity(Intent(this, CertificateActivity::class.java))
        }

        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)
        btnNotification.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                NotificationActivity::class.java
            )
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, slideInterval)

        // Refresh UI
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        siswaId = pref.getInt("id_siswa", -1)

        findViewById<TextView>(R.id.txtUser).text = nama
        findViewById<TextView>(R.id.txtNISJurusan).text = "$nisn - $kelas"

        val profileImage = findViewById<ImageView>(R.id.profile_image)
        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        // Cek apakah siswa sudah punya mitra
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", "") ?: ""
        checkMitraAndDisable(token)
        val badge = findViewById<TextView>(R.id.badgeCount)
        badge.visibility = View.GONE
        ApiClient.getInstance(this).getNotifications(nisn)
            .enqueue(object : Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {

                if (response.isSuccessful) {
                    val notifs = response.body()?.data ?: emptyList()
                    val baru = notifs.count { it.isNew }
                    if (baru > 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = baru.toString()
                    } else {
                        badge.visibility = View.GONE
                    }
                } else {
                    badge.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                badge.visibility = View.GONE
            }
        })
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun logoutUser() {
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.getInstance(this).logout().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Hapus session / SharedPreferences
                    val editor = getSharedPreferences("UserData", MODE_PRIVATE).edit()
                    editor.clear()
                    editor.apply()

                    // Redirect ke LoginActivity
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(this@MainActivity, "Berhasil logout", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Gagal logout: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkMitraAndDisable(token: String) {
        ApiClient.getInstance(this).cekPengajuanSiswa(siswaId)
            .enqueue(object : Callback<PengajuanInfoResponse> {
                override fun onResponse(
                    call: Call<PengajuanInfoResponse>,
                    response: Response<PengajuanInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val hasPengajuan = body?.has_pengajuan == true
                        val status = body?.status == "1"

                        if (hasPengajuan && status) {
                            setMenuAktif()
                        } else {
                            setMenuMati()
                        }
                    } else {
                        setMenuMati()
                    }
                }

                override fun onFailure(call: Call<PengajuanInfoResponse>, t: Throwable) {
                    setMenuMati()
                    Toast.makeText(this@MainActivity, "Gagal Mendapatkan Data Siswa", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setMenuAktif() {
        absensi.setOnClickListener {
            startActivity(Intent(this, AbsenActivity::class.java))
        }
        laporanHarian.setOnClickListener {
            startActivity(Intent(this, DailyReportActivity::class.java))
        }
        penilaianMitra.setOnClickListener {
            startActivity(Intent(this, CertificateActivity::class.java))
        }

        absensi.alpha = 1f
        laporanHarian.alpha = 1f
        penilaianMitra.alpha = 1f
    }

    private fun setMenuMati() {
        val listener = {
            val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Akses Ditolak")
                .setMessage("Menu belum bisa diakses.\nSilakan daftar PKL terlebih dahulu.")
//                .setPositiveButton("Mengerti", null)
                .show()

// Ubah warna tombol setelah dialog muncul
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
        }

        absensi.setOnClickListener { listener() }
        laporanHarian.setOnClickListener { listener() }
        penilaianMitra.setOnClickListener { listener() }

        absensi.alpha = 0.5f
        laporanHarian.alpha = 0.5f
        penilaianMitra.alpha = 0.5f
    }


}