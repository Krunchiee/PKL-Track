package com.example.pkltrack

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pkltrack.model.PenilaianResponse
import com.example.pkltrack.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CertificateActivity : AppCompatActivity() {

    private lateinit var txtScore: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtUser: TextView
    private lateinit var txtNISJurusan: TextView
    private lateinit var imgUser: ImageView
    private lateinit var btnDownload: Button

    private var sertifikatUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_certificate)

        // Init Views
        txtUser = findViewById(R.id.txtUser)
        txtNISJurusan = findViewById(R.id.txtNISJurusan)
        imgUser = findViewById(R.id.profile_image)
        txtScore = findViewById(R.id.scoreContainer)
        txtDate = findViewById(R.id.txtScoreDate)
        txtDescription = findViewById(R.id.txtDescription)
        btnDownload = findViewById(R.id.btnDownloadCertificate)

        // Load from SharedPreferences
        val pref = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama = pref.getString("nama", "User") ?: "User"
        val nisn = pref.getString("nisn", "-") ?: "-"
        val kelas = pref.getString("kelas", "-") ?: "-"
        val foto = pref.getString("foto", "")
        val idSiswa = pref.getInt("id_siswa", -1)

        val nisJurusan = "$nisn - $kelas"
        txtUser.text = nama
        txtNISJurusan.text = nisJurusan

        if (!foto.isNullOrEmpty()) {
            Glide.with(this).load(foto).circleCrop().into(imgUser)
        }

        if (idSiswa != -1) {
            fetchPenilaianData(idSiswa)
        } else {
            Toast.makeText(this, "ID Siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        btnDownload.setOnClickListener {
            if (!sertifikatUrl.isNullOrEmpty()) {
                downloadCertificateImage(sertifikatUrl!!)
            } else {
                Toast.makeText(this, "Sertifikat belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchPenilaianData(idSiswa: Int) {
        ApiClient.getInstance(this).getPenilaian(idSiswa).enqueue(object : Callback<PenilaianResponse> {
            override fun onResponse(call: Call<PenilaianResponse>, response: Response<PenilaianResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val pengajuan = response.body()?.data?.pengajuan

                    txtScore.text = pengajuan?.nilai_kompetensi ?: "-"
                    txtDescription.text = pengajuan?.catatan?.ifEmpty { "-" } ?: "-"

                    val tanggal = pengajuan?.updated_at ?: pengajuan?.created_at ?: ""
                    txtDate.text = formatTanggalIndo(tanggal)

                    sertifikatUrl = pengajuan?.sertifikat

                } else {
                    txtScore.text = "-"
                    txtDescription.text ="-"
                    txtDate.text = ""

                    sertifikatUrl = null
                    Toast.makeText(this@CertificateActivity, "Belum Ada Penilaian", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PenilaianResponse>, t: Throwable) {
                Toast.makeText(this@CertificateActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun downloadCertificateImage(imageUrl: String) {
        val uri = Uri.parse(imageUrl)

        // Ekstrak ekstensi file dari URL, misalnya .pdf atau .jpg
        val fileExtension = when {
            imageUrl.endsWith(".pdf", ignoreCase = true) -> ".pdf"
            imageUrl.endsWith(".png", ignoreCase = true) -> ".png"
            imageUrl.endsWith(".jpeg", ignoreCase = true) -> ".jpeg"
            imageUrl.endsWith(".jpg", ignoreCase = true) -> ".jpg"
            else -> ".pdf" // fallback default
        }

        // Nama file yang akan disimpan
        val fileName = "sertifikat_pkl$fileExtension"

        val request = DownloadManager.Request(uri)
            .setTitle("Sertifikat PKL")
            .setDescription("Sedang mendownload sertifikat...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Download dimulai...", Toast.LENGTH_SHORT).show()
    }


    private fun formatTanggalIndo(tanggal: String): String {
        return try {
            val cleaned = if (tanggal.length >= 10) tanggal.substring(0, 10) else tanggal
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("in", "ID"))
            val date = inputFormat.parse(cleaned)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            tanggal
        }
    }
}
