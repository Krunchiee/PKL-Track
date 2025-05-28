package com.example.pkltrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pkltrack.network.ApiClient
import com.example.pkltrack.network.ApiService
import com.example.pkltrack.network.ClockInRequest
import com.example.pkltrack.network.ClockInResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.text.Spannable
import android.text.style.ForegroundColorSpan

class ClockInActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService

    private var cancellationTokenSource = CancellationTokenSource()
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }


    private lateinit var txtTime: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtLocation: TextView
    private lateinit var edtNote: EditText
    private lateinit var btnClockIn: Button
    private lateinit var btnRefresh: ImageView
    private lateinit var btnBack: ImageView

    private var currentGeo: GeoPoint? = null

    private var centerLocation: GeoPoint? = null
    private var allowedRadiusMeters: Double = 1000.0

    companion object {
        private const val REQ_LOCATION = 1001
        private val localeID = Locale("in", "ID")
        private val timeFmt = SimpleDateFormat("HH.mm", localeID)
        private val dateFmt = SimpleDateFormat("yyyy-MM-dd", localeID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*  OSMDroid requires context config before setContentView  */
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        setContentView(R.layout.activity_clock_in)

        // ---- View binding ----
        mapView     = findViewById(R.id.mapView)
        txtTime     = findViewById(R.id.clockInTitle)
        txtStatus   = findViewById(R.id.status)
        txtLocation = findViewById(R.id.locationText)
        edtNote     = findViewById(R.id.noteField)
        btnClockIn  = findViewById(R.id.btnClockIn)
        btnRefresh  = findViewById(R.id.btnRefresh)
        btnBack     = findViewById(R.id.btnBack)

        // ---- SharedPreferences ----
        prefs = getSharedPreferences("ClockInPrefs", Context.MODE_PRIVATE)

        // ---- Retrofit ----
        apiService = ApiClient.service
        fetchCenterLocation()

        // ---- Map init ----
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // ---- Fused Location ----
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ---- Time ----
        txtTime.text = timeFmt.format(Date())

        // ---- Listeners ----
        btnRefresh.setOnClickListener { refreshLocation()}
        btnClockIn.setOnClickListener { saveAndSendClockIn() }
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // ---- Initial location ----
        refreshLocation()
    }

    /* -------------------- LOCATION -------------------- */
    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION)
    }

    private fun fetchCenterLocation() {
        centerLocation = GeoPoint(-6.200000, 106.816666)
    }

    private fun isWithinRadius(current: GeoPoint, center: GeoPoint, radiusMeters: Double): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            current.latitude, current.longitude,
            center.latitude, center.longitude,
            results
        )
        return results[0] <= radiusMeters
    }

    private fun refreshLocation() {
        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }


    @SuppressLint("MissingPermission")
//    private fun getCurrentLocation()  {
//        val currentTask: Task<Location> = fusedLocationProviderClient.getCurrentLocation(
//            PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
//        )
//        currentTask.addOnCompleteListener { task ->
//            if (task.isSuccessful && task.result != null) {
//                val result = task.result
//                val address = runBlocking {getAddressFromCoordinates(result.latitude, result.longitude)}
//
//                currentGeo = GeoPoint(result.latitude, result.longitude)
//                txtLocation.text = "${address}"
//                updateMap(currentGeo!!, "Lokasi Anda")
//                drawRadiusCircle(currentGeo!!, allowedRadiusMeters)
//            } else {
//                    txtLocation.text = "Lokasi tidak ditemukan"
//                }
//            }.addOnFailureListener {
//                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun getCurrentLocation() {
        val currentTask = fusedLocationProviderClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
        )

        currentTask.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val loc = task.result
                currentGeo = GeoPoint(loc.latitude, loc.longitude)

                // --- alamat via reverse geocode ---
                lifecycleScope.launch {
                    val address =
                        withContext(Dispatchers.IO) { getAddressFromCoordinates(loc.latitude, loc.longitude) }
                            ?: "${loc.latitude}, ${loc.longitude}"

                    // --- cek radius (jika centerLocation tersedia) ---
                    val inside = centerLocation?.let {
                        isWithinRadius(currentGeo!!, it, allowedRadiusMeters)
                    } ?: true   // jika center belum ada, anggap di dalam

                    // build string + warna
                    if (inside) {
                        txtLocation.text = address

                    } else {
                        val warning = " | Di Luar Radius (1 KM)"
                        val fullText = "$address$warning"
                        val spannable = SpannableString(fullText)
                        spannable.setSpan(
                            ForegroundColorSpan(Color.parseColor("#FF0000")),
                            address.length,                   // mulai dari sini
                            fullText.length,                  // sampai akhir
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        txtLocation.text = spannable
                    }

                    // update peta & lingkaran 1 km di posisi user
                    updateMap(currentGeo!!, "Lokasi Anda")
                    drawRadiusCircle(currentGeo!!, allowedRadiusMeters)
                }
            } else {
                txtLocation.text = "Lokasi tidak ditemukan"
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude"
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val json = JSONObject(it)
                    return@withContext json.getString("display_name")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshLocation()
        } else {
            Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawRadiusCircle(center: GeoPoint, radiusMeters: Double) {
        val circle = Polygon()
        circle.points = Polygon.pointsAsCircle(center, radiusMeters)
        circle.fillPaint.color = ContextCompat.getColor(this, R.color.transparent_light_green) // Buat warna transparan
        circle.strokeColor = ContextCompat.getColor(this, R.color.transparent_dark_green)
        circle.strokeWidth = 2f
        circle.title = "Area Absen"

        mapView.overlays.add(circle)
        mapView.invalidate()
    }

    /* -------------------- MAP helpers -------------------- */
    private fun updateMap(point: GeoPoint, title: String) {
        mapView.overlays.clear()
        addMarker(point, title)
        mapView.controller.setCenter(point)
    }

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    /* -------------------- CLOCK IN -------------------- */
    private fun saveAndSendClockIn() {
        val geo = currentGeo
        if (geo == null) {
            Toast.makeText(this, "Lokasi belum tersedia", Toast.LENGTH_SHORT).show(); return
        }

        if (centerLocation == null) {
            Toast.makeText(this, "Lokasi pusat belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        // 👉 Validasi radius
        if (!isWithinRadius(geo, centerLocation!!, allowedRadiusMeters)) {
            Toast.makeText(this, "Kamu di luar radius absen (1 km)", Toast.LENGTH_LONG).show()
            txtStatus.text = "Di Luar Area Absen"
            txtStatus.setTextColor(Color.RED)
            return
        }


        val time = txtTime.text.toString()
        val date = dateFmt.format(Date())
        val note = edtNote.text.toString()

        // Simpan lokal
        with(prefs.edit()) {
            putString("time", time)
            putString("date", date)
            putString("location", txtLocation.text.toString())
            putString("note", note)
            apply()
        }

        // Kirim ke API
        val body = ClockInRequest(
            latitude = geo.latitude,
            longitude = geo.longitude,
            date = date,
            time = time,
            note = note
        )

        txtStatus.text = "Sending…"
        lifecycleScope.launch {
            try {
                val resp: ClockInResponse = withContext(Dispatchers.IO) { apiService.postClockIn(body) }
                txtStatus.text = "Clocked In (Server)"
                txtStatus.setTextColor(ContextCompat.getColor(this@ClockInActivity, R.color.light_green))
                Toast.makeText(this@ClockInActivity, "Clock In berhasil di server", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                txtStatus.text = "Clock In gagal (${e.code()})"
            } catch (e: Exception) {
                txtStatus.text = "Clock In gagal (Network)"
            }
        }
    }

    /* -------------------- Lifecycle for MapView -------------------- */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
