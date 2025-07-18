package com.example.pkltrack

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.example.pkltrack.network.ApiClient
import com.example.pkltrack.util.FileUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DailyReportActivity : AppCompatActivity() {

    private lateinit var imgPlaceholder: ImageView
    private lateinit var edtDescription: EditText
    private lateinit var btnKirim: Button
    private var selectedUri: Uri? = null

    private fun hasReadImagePerm(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageLauncher.launch("image/*")
            else Toast.makeText(this, "Izin akses gambar ditolak", Toast.LENGTH_SHORT).show()
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedUri = it
                imgPlaceholder.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_daily_report)

        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        val idSiswa = pref.getInt("id_siswa", -1)

        findViewById<TextView>(R.id.txtUser).text = nama
        findViewById<TextView>(R.id.txtNISJurusan).text = "$nisn - $kelas"
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(profileImage)
        }

        imgPlaceholder = findViewById(R.id.imagePlaceholder)
        edtDescription = findViewById(R.id.editDescription)
        btnKirim = findViewById(R.id.btnKirim)

        imgPlaceholder.setOnClickListener {
            if (hasReadImagePerm()) pickImageLauncher.launch("image/*")
            else {
                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
                permissionLauncher.launch(perm)
            }
        }

        btnKirim.setOnClickListener { performSubmit(idSiswa) }
    }

    private fun performSubmit(idSiswa: Int) {
        val description = edtDescription.text.toString().trim()
        val idSiswaPart = idSiswa.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        if (selectedUri == null) {
            Toast.makeText(this, "Silakan pilih gambar dokumentasi!", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi kegiatan belum diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val file = FileUtil.getFileFromUri(this, selectedUri!!)
        if (file == null) {
            Toast.makeText(this, "Gagal memproses file gambar!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        ApiClient.getInstance(this)
            .uploadLaporan(idSiswa = idSiswaPart, photo = imagePart, keterangan = descriptionPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@DailyReportActivity,
                            "Laporan berhasil dikirim!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@DailyReportActivity,
                            "Gagal mengirim laporan!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@DailyReportActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
