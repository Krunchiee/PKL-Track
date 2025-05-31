package com.example.pkltrack

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

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

    private val images = listOf(
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                R.id.nav_form -> {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        //untuk membawa data ke header
        val pref       = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama   = pref.getString("nama", "User")
        val nisn = pref.getString("nisn", "00000000")
        val kelas = pref.getString("kelas", "XI RPL 1")
        val foto = pref.getString("foto", "foto user")
        val id_siswa = pref.getInt("id_siswa", 2)

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisn+" - "+kelas
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        Glide.with(this).load(foto).into(profileImage)

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
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, slideInterval)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}