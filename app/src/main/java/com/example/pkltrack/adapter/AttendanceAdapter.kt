package com.example.pkltrack.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pkltrack.R
import com.example.pkltrack.model.Attendance

class AttendanceAdapter(private val attendanceList: List<Attendance>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val icAttendance: ImageView = itemView.findViewById(R.id.icAttendance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val item = attendanceList[position]
        holder.txtDate.text = item.date
        holder.txtTime.text = "${item.clockIn} - ${item.clockOut}"

        if (item.isLate) {
            holder.txtTime.setTextColor(Color.RED)
        } else {
            holder.txtTime.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = attendanceList.size
}
