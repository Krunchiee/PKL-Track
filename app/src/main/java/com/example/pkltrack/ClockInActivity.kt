package com.example.pkltrack

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class ClockInActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: SharedPreferences

    private lateinit var txtTime: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtLocation: TextView
    private lateinit var edtNote: EditText
    private lateinit var btnClockIn: Button
    private lateinit var btnRefresh: ImageView
    private lateinit var btnBack: ImageView

    companion object {
        private const val REQ_LOCATION = 1001
        private val localeID = Locale("in", "ID")
        private val timeFmt = SimpleDateFormat("HH.mm", localeID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock_in)

        // View Binding
        txtTime     = findViewById(R.id.clockInTitle)
        txtStatus   = findViewById(R.id.status)
        txtLocation = findViewById(R.id.locationText)
        edtNote     = findViewById(R.id.noteField)
        btnClockIn  = findViewById(R.id.btnClockIn)
        btnRefresh  = findViewById(R.id.btnRefresh)
        btnBack     = findViewById(R.id.btnBack)

        // Shared Preferences
        prefs = getSharedPreferences("ClockInPrefs", Context.MODE_PRIVATE)

        // Map & Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Time
        txtTime.text = timeFmt.format(Date())

        // Event listeners
        btnRefresh.setOnClickListener { refreshLocation() }
        btnClockIn.setOnClickListener { saveClockIn() }
        btnBack.setOnClickListener { finish() }

        // Initial Location
        refreshLocation()
    }

    // ---------------------- LOCATION ----------------------
    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQ_LOCATION
        )
    }

    private fun refreshLocation() {
        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                updateMap(latLng, "Lokasi Anda")
                txtLocation.text = "${location.latitude}, ${location.longitude}"
            } else {
                txtLocation.text = "Lokasi tidak ditemukan"
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------- MAP ----------------------
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Fallback default location (Monas)
        val defaultLatLng = LatLng(-6.175392, 106.827153)
        updateMap(defaultLatLng, "Monas")
    }

    private fun updateMap(latLng: LatLng, title: String) {
        map.clear()
        map.addMarker(MarkerOptions().position(latLng).title(title))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    // ---------------------- CLOCK IN ----------------------
    private fun saveClockIn() {
        val time = txtTime.text.toString()
        val location = txtLocation.text.toString()
        val note = edtNote.text.toString()

        with(prefs.edit()) {
            putString("time", time)
            putString("location", location)
            putString("note", note)
            apply()
        }

        txtStatus.text = "Clocked In"
        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.light_green))
        Toast.makeText(this, "Clock In berhasil!", Toast.LENGTH_SHORT).show()
    }
}
