package com.example.pkltrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pkltrack.model.Mitra
import com.example.pkltrack.R

class MitraAdapter(
    private val list: List<Mitra>,
    private val onItemClick: (Mitra) -> Unit
) : RecyclerView.Adapter<MitraAdapter.MitraViewHolder>() {

    inner class MitraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNama: TextView = itemView.findViewById(R.id.txtNamaMitra)

        init {
            itemView.setOnClickListener {
                onItemClick(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MitraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mitra, parent, false)
        return MitraViewHolder(view)
    }

    override fun onBindViewHolder(holder: MitraViewHolder, position: Int) {
        holder.txtNama.text = list[position].nama_mitra
    }

    override fun getItemCount(): Int = list.size
}
