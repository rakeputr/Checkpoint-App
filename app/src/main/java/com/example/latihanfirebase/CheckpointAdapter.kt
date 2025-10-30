package com.example.latihanfirebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

// Adapter menerima list cp dan listener sebagai parameter
class CheckpointAdapter(
    private val items: List<Checkpoint>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<CheckpointAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val desc: TextView = view.findViewById(R.id.tvDescription)
        val tanggal: TextView = view.findViewById(R.id.tvTanggal)
        val coords: TextView = view.findViewById(R.id.tvCoordinates)
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

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

        // Konversi objek Date menjadi String
        holder.tanggal.text = dateFormat.format(item.timestamp)

        holder.btnOpenMap.setOnClickListener {
            listener.onMapClick(item)
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(item) // Panggil fungsi onItemClick pada listener
        }
    }
}