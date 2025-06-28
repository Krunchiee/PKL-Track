package com.example.pkltrack.model

import com.google.gson.annotations.SerializedName

data class Pengajuan(
    val pembimbing: Pembimbing?
)

data class Pembimbing(
    val nama: String
)

data class NotificationItem(
    val tgl_bimbingan: String,
    val pembimbing: String,
    val keterangan: String,
    val pengajuan: Pengajuan?, // ← Tambahkan ini
    @SerializedName("is_new") val isNewRaw: String
) {
    val isNew: Boolean
        get() = isNewRaw.equals("true", ignoreCase = true)
}
