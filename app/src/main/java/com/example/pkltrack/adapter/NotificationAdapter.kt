package com.example.pkltrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pkltrack.model.NotificationItem
import com.example.pkltrack.R
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(private val list: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        val txtTanggalJam: TextView = view.findViewById(R.id.txtTanggalJam)
        val txtPembimbing: TextView = view.findViewById(R.id.txtPembimbing)
        val txtKeterangan: TextView = view.findViewById(R.id.txtKeterangan)
        val layout: LinearLayout = view.findViewById(R.id.notificationItemLayout)
    }

    fun formatTanggalBimbingan(original: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(original)
            "${outputFormat.format(date)} WIB"
        } catch (e: Exception) {
            original // fallback jika error parsing
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        holder.txtTitle.text = "Jadwal Bimbingan"
        holder.txtTanggalJam.text = "Tanggal & Jam: ${formatTanggalBimbingan(item.tgl_bimbingan)}"
        holder.txtPembimbing.text = "Pembimbing: ${item.pengajuan?.pembimbing?.nama ?: "-"}"
        holder.txtKeterangan.text = "Keterangan: ${item.keterangan}"

        if (item.isNew) {
            // Set warna awal abu-abu
            val startColor = ContextCompat.getColor(context, R.color.dark_gray)
            val endColor = ContextCompat.getColor(context, android.R.color.white)

            holder.layout.setBackgroundColor(startColor)

            // Bold teks
            holder.txtTanggalJam.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.txtPembimbing.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.txtKeterangan.setTypeface(null, android.graphics.Typeface.BOLD)

            // Gunakan ValueAnimator untuk transisi warna halus
            val colorAnimation = android.animation.ValueAnimator.ofArgb(startColor, endColor)
            colorAnimation.duration = 1800 // durasi animasi dalam ms
            colorAnimation.startDelay = 500 // tunggu sebentar sebelum mulai transisi
            colorAnimation.addUpdateListener { animator ->
                holder.layout.setBackgroundColor(animator.animatedValue as Int)
            }
            colorAnimation.start()
        } else {
            // Set normal background dan normal text style
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            holder.txtTanggalJam.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.txtPembimbing.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.txtKeterangan.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }



    override fun getItemCount(): Int = list.size
}

