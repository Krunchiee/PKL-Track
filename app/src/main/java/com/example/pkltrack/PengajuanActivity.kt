package com.example.pkltrack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pkltrack.model.*
import com.example.pkltrack.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

class PengajuanActivity : AppCompatActivity() {

    private var cvFileUri: Uri? = null
    private var selectedJurusanId: Int? = null
    private var selectedMitraId: Int? = null
    private var siswaId: Int? = null

    private lateinit var spinnerJurusan: Spinner
    private lateinit var txtFileName: TextView
    private lateinit var btnUploadCV: Button
    private lateinit var btnKirim: Button

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val mimeType = contentResolver.getType(uri)
                if (mimeType == "application/pdf") {
                    cvFileUri = uri
                    txtFileName.text = getFileName(uri)
                    txtFileName.setTextColor(resources.getColor(android.R.color.black))
                } else {
                    Toast.makeText(this, "Hanya file PDF yang diperbolehkan", Toast.LENGTH_SHORT).show()
                    txtFileName.text = "Belum ada file dipilih"
                    txtFileName.setTextColor(resources.getColor(android.R.color.darker_gray))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengajuan)

        //untuk membawa data ke header
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        val idSiswa = pref.getInt("id_siswa", -1)

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisn+" - "+kelas
        val profileImage = findViewById<ImageView>(R.id.profile_image)

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        // Init Views
        spinnerJurusan = findViewById(R.id.spinnerKeahlian)
        txtFileName = findViewById(R.id.txtFileName)
        btnUploadCV = findViewById(R.id.btnUploadCV)
        btnKirim = findViewById(R.id.btnKirim)

        // Get siswaId
        siswaId = pref.getInt("id_siswa", -1)
        if (siswaId == 0) {
            Toast.makeText(this, "Error: ID Siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get mitra ID from Intent
        selectedMitraId = intent.getIntExtra("id_mitra", 0)
        if (selectedMitraId == 0) {
            Toast.makeText(this, "Error: ID Mitra tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Event listeners
        btnUploadCV.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }

        btnKirim.setOnClickListener {
            if (validateForm()) {
                submitPengajuan()
            }
        }

        // Load Jurusan
        loadJurusanData()
    }

    private fun validateForm(): Boolean {
        if (selectedJurusanId == null) {
            Toast.makeText(this, "Pilih konsentrasi keahlian terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cvFileUri == null) {
            Toast.makeText(this, "Pilih file CV dalam format PDF", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedMitraId == null || selectedMitraId == 0) {
            Toast.makeText(this, "ID Mitra tidak valid", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun submitPengajuan() {
        try {
            val cvFile = createFileFromUri(cvFileUri!!)
            if (cvFile == null) {
                Toast.makeText(this, "Gagal membaca file CV", Toast.LENGTH_SHORT).show()
                return
            }

            val requestFile = cvFile.asRequestBody("application/pdf".toMediaTypeOrNull())
            val cvPart = MultipartBody.Part.createFormData("berkas_cv", cvFile.name, requestFile)

            val siswaIdBody = siswaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val mitraIdBody = selectedMitraId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", "") ?: ""

            // Tampilkan loading sementara (opsional)
            Toast.makeText(this, "Mengirim pengajuan...", Toast.LENGTH_SHORT).show()

            ApiClient.getInstance(this).submitPengajuan(
               siswaIdBody, mitraIdBody, cvPart
            ).enqueue(object : Callback<PengajuanResponse> {
                override fun onResponse(call: Call<PengajuanResponse>, response: Response<PengajuanResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@PengajuanActivity,
                            response.body()?.message ?: "Pengajuan berhasil!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Delay 1.5 detik agar data pengajuan tersimpan dulu di server
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this@PengajuanActivity, StatusPengajuanActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }, 1500)

                    } else {
                        val errorBody = response.errorBody()?.string()
                        try {
                            val json = JSONObject(errorBody)
                            val message = json.getString("message")
                            Toast.makeText(this@PengajuanActivity, "$message", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@PengajuanActivity, "Gagal: $errorBody", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PengajuanResponse>, t: Throwable) {
                    Toast.makeText(this@PengajuanActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "temp_cv_${System.currentTimeMillis()}.pdf")
            val outputStream = file.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }

    private fun loadJurusanData() {
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", "") ?: ""

        ApiClient.getInstance(this).getListJurusan()
            .enqueue(object : Callback<JurusanListResponse> {
                override fun onResponse(call: Call<JurusanListResponse>, response: Response<JurusanListResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val jurusanList = response.body()?.data ?: emptyList()
                        val namaList = mutableListOf("Pilih Konsentrasi")
                        namaList.addAll(jurusanList.map { it.nama_jurusan })
                        val idMap = jurusanList.associateBy { it.nama_jurusan }

                        val adapter = ArrayAdapter(this@PengajuanActivity, android.R.layout.simple_spinner_dropdown_item, namaList)
                        spinnerJurusan.adapter = adapter

                        spinnerJurusan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                                val selectedName = namaList[position]
                                selectedJurusanId = idMap[selectedName]?.id
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                selectedJurusanId = null
                            }
                        }
                    } else {
                        Toast.makeText(this@PengajuanActivity, "Gagal memuat jurusan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JurusanListResponse>, t: Throwable) {
                    Toast.makeText(this@PengajuanActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
