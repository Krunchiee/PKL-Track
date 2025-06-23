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
import com.example.pkltrack.model.ClockInRequest
import com.example.pkltrack.model.ClockInResponse
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
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.example.pkltrack.model.KoordinatResponse
import com.example.pkltrack.model.ServerTimeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ClockInActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prefs: SharedPreferences
    private lateinit var apiService: ApiService
    private var serverDate: Date? = null

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
//        apiService = ApiClient.service
        fetchCenterLocation()

        // ---- Map init ----
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // ---- Fused Location ----
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ---- Time ----
        fetchServerTime()

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
        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val idSiswa = userPrefs.getInt("id_siswa", 0)

        val body = mapOf("id_siswa" to idSiswa)

        ApiClient.getInstance(this).getKoordinat(body).enqueue(object : Callback<KoordinatResponse> {
            override fun onResponse(call: Call<KoordinatResponse>, response: Response<KoordinatResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    centerLocation = GeoPoint(data?.lat?.toDouble() ?: 0.0, data?.lng?.toDouble() ?: 0.0)
                } else {
                    Toast.makeText(this@ClockInActivity, "Gagal mengambil pusat absen", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<KoordinatResponse>, t: Throwable) {
                Toast.makeText(this@ClockInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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


        val now = serverDate ?: Date() // fallback pakai Date() jika gagal ambil server time
        val time = timeFmt.format(now)
        val date = dateFmt.format(now)
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
        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val idSiswa = userPrefs.getInt("id_siswa", 0)

        val clockInRequest = ClockInRequest(
            idSiswa = idSiswa,
            tanggal = date,
            lat = geo.latitude,
            lng = geo.longitude,
            status = "Hadir",
            keterangan = note
        )

        txtStatus.text = "Mengirim..."
        ApiClient.getInstance(this).postClockIn(clockInRequest).enqueue(object : Callback<ClockInResponse> {
            override fun onResponse(call: Call<ClockInResponse>, response: Response<ClockInResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    txtStatus.text = "Berhasil Clock In"
                    txtStatus.setTextColor(ContextCompat.getColor(this@ClockInActivity, R.color.light_green))
                    Toast.makeText(this@ClockInActivity, "Clock In berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    txtStatus.text = "Gagal Clock In"
                }
            }

            override fun onFailure(call: Call<ClockInResponse>, t: Throwable) {
                txtStatus.text = "Error saat Clock In"
                Toast.makeText(this@ClockInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    /* -------------------- Time From Server -------------------- */
    private fun fetchServerTime() {
        ApiClient.getInstance(this).getServerTime()
            .enqueue(object : Callback<ServerTimeResponse> {
                override fun onResponse(call: Call<ServerTimeResponse>, response: Response<ServerTimeResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverTimeStr = response.body()!!.data.server_time // "2025-06-23 14:28:26"
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val parsed = sdf.parse(serverTimeStr)
                        if (parsed != null) {
                            serverDate = parsed
                            txtTime.text = timeFmt.format(parsed) // tampilkan jam dari server
                        }
                    } else {
                        Toast.makeText(this@ClockInActivity, "Gagal ambil waktu server", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ServerTimeResponse>, t: Throwable) {
                    Toast.makeText(this@ClockInActivity, "Error ambil waktu server", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
