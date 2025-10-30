package com.example.latihanfirebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.latihanfirebase.DetailActivity


interface OnItemClickListener {
    fun onMapClick(checkpoint: Checkpoint)
    fun onItemClick(checkpoint: Checkpoint)
}

class HomeActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CheckpointAdapter
    private val checkpointList = mutableListOf<Checkpoint>()
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.rvCheckpoints)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)
        val btnLogout: ImageButton = findViewById(R.id.btnLogout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CheckpointAdapter(checkpointList, this)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddLocationActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadCheckpoints()
    }

    private fun loadCheckpoints() {
        // user?.uid?.let memastikan hanya pengguna yang terotentikasi yang dicari
        user?.uid?.let { uid ->
            db.collection("checkpoints")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { result ->
                    checkpointList.clear()

                    if (result.isEmpty) {
                        Toast.makeText(this, "Anda belum menambahkan checkpoint.", Toast.LENGTH_SHORT).show()
                    }

                    // perulangan buat munculin cpnya
                    for (doc in result) {
                        try {
                            val cp = doc.toObject(Checkpoint::class.java)

                            // ambil id dokumen buat diteruskan ke detail activiti
                            cp.id = doc.id

                            // nambah kalo name not null
                            if (cp.name != null) {
                                checkpointList.add(cp)
                            }
                        } catch (e: Exception) {
                            // Penanganan error jika konversi toObject gagal
                            Toast.makeText(this, "Gagal memproses satu data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // ngasi tahu Adapter bahwa data telah berubah
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // misal ga ke load muncul pesan ini
                    Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } ?: run {
            Toast.makeText(this, "Anda harus login untuk memuat data.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCheckpoints()
    }

    override fun onItemClick(checkpoint: Checkpoint) {
        checkpoint.id?.let { id ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_CHECKPOINT_ID, id)
            startActivity(intent)
        } ?: Toast.makeText(this, "ID Checkpoint hilang.", Toast.LENGTH_SHORT).show()
    }

    override fun onMapClick(checkpoint: Checkpoint) {
        val lat = checkpoint.latitude
        val lng = checkpoint.longitude

        if (lat != null && lng != null) {
            // Membuat URI geo:latitude,longitude?q=label
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=${Uri.encode(checkpoint.name)}")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Aplikasi Google Maps tidak terinstal. Mencoba membuka di browser.", Toast.LENGTH_SHORT).show()
                // Fallback ke browser jika Google Maps tidak ada
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?q=$lat,$lng"))
                startActivity(webIntent)
            }
        } else {
            Toast.makeText(this, "Koordinat GPS tidak tersedia untuk lokasi ini.", Toast.LENGTH_SHORT).show()
        }
    }
}