package com.example.pkltrack

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pkltrack.model.ProfileResponse
import com.example.pkltrack.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        imgFoto = findViewById(R.id.imgFoto)
        edtNama = findViewById(R.id.editNama)
        edtNISN = findViewById(R.id.editNISN)
        edtKelas = findViewById(R.id.editKelas)
        edtAlamat = findViewById(R.id.editAlamat)
        edtNoHp = findViewById(R.id.editNoHp)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Ambil data dari API
        loadProfile()

        // Pilih gambar dari galeri
        imgFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.getInstance(this).getProfile("Bearer $token").enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()!!
                    val siswa = user.siswa

                    siswaId = siswa.id
                    edtNama.setText(siswa.nama)
                    edtNISN.setText(siswa.nisn)
                    edtKelas.setText(siswa.kelas)
                    edtAlamat.setText(siswa.alamat)
                    edtNoHp.setText(siswa.no_hp)

                        val fotoUrl = "https://pkltrack.my.id/storage/foto/${siswa.foto}"
                    Glide.with(this@ProfileActivity)
                        .load(fotoUrl)
                        .into(imgFoto)


                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile() {
        val token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", null) ?: return

        val nama = edtNama.text.toString()
        val nisn = edtNISN.text.toString()
        val kelas = edtKelas.text.toString()
        val alamat = edtAlamat.text.toString()
        val noHp = edtNoHp.text.toString()

        val namaPart = RequestBody.create("text/plain".toMediaTypeOrNull(), nama)
        val nisnPart = RequestBody.create("text/plain".toMediaTypeOrNull(), nisn)
        val kelasPart = RequestBody.create("text/plain".toMediaTypeOrNull(), kelas)
        val alamatPart = RequestBody.create("text/plain".toMediaTypeOrNull(), alamat)
        val noHpPart = RequestBody.create("text/plain".toMediaTypeOrNull(), noHp)

        val fotoPart: MultipartBody.Part? = selectedImageFile?.let {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData("foto", it.name, requestFile)
        }


        ApiClient.getInstance(this).updateProfile(
            id = siswaId,
            token = "Bearer $token",
            nisn = nisnPart,
            nama = namaPart,
            kelas = kelasPart,
            alamat = alamatPart,
            no_hp = noHpPart,
            foto = fotoPart
        ).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            imgFoto.setImageURI(selectedImageUri)

            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImageUri, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
            val picturePath = columnIndex?.let { cursor.getString(it) }
            selectedImageFile = File(picturePath ?: "")
            cursor?.close()
        }
    }
}
