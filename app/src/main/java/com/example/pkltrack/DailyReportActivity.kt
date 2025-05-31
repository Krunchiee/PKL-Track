package com.example.pkltrack

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class DailyReportActivity : AppCompatActivity() {

    // --- view refs ---
    private lateinit var imgPlaceholder: ImageView
    private lateinit var edtDescription: EditText
    private lateinit var btnKirim: Button

    // --- state ---
    private var selectedUri: Uri? = null

    /* ---------- runtime permission helpers ---------- */

    /** true if we already have permission to read images */
    private fun hasReadImagePerm(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: READ_MEDIA_IMAGES
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

    /** permission launcher */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageLauncher.launch("image/*")
            else Toast.makeText(this, "Izin akses gambar ditolak", Toast.LENGTH_SHORT).show()
        }

    /* ---------- gallery picker ---------- */

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedUri = it
                imgPlaceholder.setImageURI(it)          // preview
            }
        }

    /* ---------- lifecycle ---------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_report)

        //untuk membawa data ke header
        val pref       = getSharedPreferences("UserData", MODE_PRIVATE)
        val nama   = pref.getString("nama", "User")
        val nisJurusan = pref.getString("nisJurusan", "00000000 - Jurusan")

        findViewById<TextView>(R.id.txtUser).text        = nama
        findViewById<TextView>(R.id.txtNISJurusan).text  = nisJurusan

        // find views
        imgPlaceholder = findViewById(R.id.imagePlaceholder)
        edtDescription = findViewById(R.id.editDescription)
        btnKirim = findViewById(R.id.btnKirim)

        // pick image when placeholder clicked
        imgPlaceholder.setOnClickListener {
            if (hasReadImagePerm()) pickImageLauncher.launch("image/*")
            else {
                // request proper permission depending on API level
                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
                permissionLauncher.launch(perm)
            }
        }

        // send data
        btnKirim.setOnClickListener { performSubmit() }
    }

    /* ---------- submit ---------- */

    private fun performSubmit() {
        val description = edtDescription.text.toString().trim()

        if (selectedUri == null) {
            Toast.makeText(this, "Silakan pilih gambar dokumentasi!", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi kegiatan belum diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // 👉 convert Uri to file-path or multipart as needed
        // (contoh sederhana: hanya menampilkan Toast)
        Toast.makeText(
            this,
            "Gambar: ${getFileName(selectedUri!!)}\nDeskripsi: $description",
            Toast.LENGTH_LONG
        ).show()

        // TODO: panggil API upload di sini menggunakan Retrofit / Volley, dll.
    }

    /* ---------- helper to get filename from Uri (optional) ---------- */
    private fun getFileName(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndexOrThrow(projection[0])
            cursor.moveToFirst()
            return cursor.getString(nameIdx)
        }
        return uri.lastPathSegment ?: "image.jpg"
    }
}
