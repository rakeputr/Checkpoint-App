package com.example.latihanfirebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Ganti dari ImageButton ke Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. Interface Listener tetap sama
interface OnItemClickListener {
    fun onMapClick(checkpoint: Checkpoint)
}

// 2. Adapter menerima listener sebagai parameter
class CheckpointAdapter(
    private val items: List<Checkpoint>,
    private val listener: OnItemClickListener // Tambahkan listener
) :
    RecyclerView.Adapter<CheckpointAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val desc: TextView = view.findViewById(R.id.tvDescription)
        val coords: TextView = view.findViewById(R.id.tvCoordinates)
        // Ubah tipe variabel menjadi Button
        val btnOpenMap: Button = view.findViewById(R.id.btnOpenMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkpoint, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.desc.text = item.description
        holder.coords.text = "Lat: ${item.latitude}, Lng: ${item.longitude}"

        // 3. Set Click Listener pada tombol
        holder.btnOpenMap.setOnClickListener {
            listener.onMapClick(item)
        }
    }
}