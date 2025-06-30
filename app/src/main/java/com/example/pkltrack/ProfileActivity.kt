package com.example.pkltrack

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pkltrack.model.ProfileResponse
import com.example.pkltrack.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgFoto: ImageView
    private lateinit var edtNama: EditText
    private lateinit var edtNISN: EditText
    private lateinit var edtKelas: EditText
    private lateinit var edtAlamat: EditText
    private lateinit var edtNoHp: EditText
    private lateinit var btnSimpan: Button
    private lateinit var selectedImageUri: Uri
    private var selectedImageFile: File? = null
    private var siswaId: Int = -1

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        imgFoto = findViewById(R.id.imgFoto)
        edtNama = findViewById(R.id.editNama)
        edtNISN = findViewById(R.id.editNISN)
        edtKelas = findViewById(R.id.editKelas)
        edtAlamat = findViewById(R.id.editAlamat)
        edtNoHp = findViewById(R.id.editNoHp)
        btnSimpan = findViewById(R.id.btnSimpan)

        loadProfile()

        imgFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSimpan.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", null)
        val idSiswa = getSharedPreferences("UserData", MODE_PRIVATE).getInt("id_siswa", -1)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.getInstance(this).getProfile(idSiswa).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    val siswa = profile?.data

                    if (siswa != null) {
                        // Fill your UI
                        edtNama.setText(siswa.nama)
                        edtNISN.setText(siswa.nisn)
                        edtKelas.setText(siswa.kelas.trim()) // trim to remove tabs
                        edtAlamat.setText(siswa.alamat)
                        edtNoHp.setText(siswa.no_hp)

                        // Save ID for update
                        siswaId = siswa.id

                        // Load photo
                        val fotoUrl = "https://pkltrack.my.id/${siswa.foto}"
                        Glide.with(this@ProfileActivity)
                            .load(fotoUrl)
                            .circleCrop()
                            .into(imgFoto)

                        // Simpan ke SharedPreferences
                        val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("nama", siswa.nama)
                            putString("nisn", siswa.nisn)
                            putString("kelas", siswa.kelas)
                            putString("alamat", siswa.alamat)
                            putString("no_hp", siswa.no_hp)
                            putString("foto", fotoUrl)
                            apply()
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "Data Siswa Kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile() {
        val nama = edtNama.text.toString()
        val nisn = edtNISN.text.toString()
        val kelas = edtKelas.text.toString()
        val alamat = edtAlamat.text.toString()
        val noHp = edtNoHp.text.toString()

        // Buat part map
        val map = HashMap<String, RequestBody>().apply {
            put("nama", nama.toRequestBody("text/plain".toMediaType()))
            put("nisn", nisn.toRequestBody("text/plain".toMediaType()))
            put("kelas", kelas.toRequestBody("text/plain".toMediaType()))
            put("alamat", alamat.toRequestBody("text/plain".toMediaType()))
            put("no_hp", noHp.toRequestBody("text/plain".toMediaType()))
        }

        // Buat part foto jika tersedia
        val fotoPart = selectedImageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("foto", it.name, requestFile)
        }

        ApiClient.getInstance(this).updateProfile(
            id = siswaId,
            partMap = map,
            foto = fotoPart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val result = response.body()?.string()
                    Log.d("UPDATE_SUCCESS", "Body: $result")
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()

                    loadProfile()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UPDATE_FAILED", "Error Body: $errorBody")
                    Toast.makeText(this@ProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_RESPONSE", "Kesalahan jaringan: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            imgFoto.setImageURI(selectedImageUri)

            val inputStream = contentResolver.openInputStream(selectedImageUri)
            val file = File(cacheDir, "selected_image.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            selectedImageFile = file

        }
    }
}
