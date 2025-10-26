package com.example.latihanfirebase

import android.content.Intent
import android.net.Uri // Import Uri untuk Intent Map
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// 1. Implementasikan interface OnItemClickListener
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
        // 2. Berikan 'this' (HomeActivity) sebagai listener ke Adapter
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
        val TAG = "CHECKPOINT_APP"

        user?.uid?.let { uid ->
            Log.d(TAG, "Memulai query data untuk UID: $uid")

            db.collection("checkpoints")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { result ->

                    Log.d(TAG, "Query berhasil. Ditemukan ${result.size()} dokumen.")

                    if (result.isEmpty) {
                        Log.w(TAG, "Tidak ada data checkpoint ditemukan.")
                        Toast.makeText(this, "Tidak ada data checkpoint.", Toast.LENGTH_SHORT).show()
                    }

                    checkpointList.clear()
                    var successCount = 0
                    var failCount = 0

                    for (doc in result) {
                        try {
                            Log.v(TAG, "Data Dokumen mentah: ${doc.data}")

                            val cp = doc.toObject(Checkpoint::class.java)

                            if (cp.name != null) {
                                checkpointList.add(cp)
                                successCount++
                            } else {
                                Log.e(TAG, "toObject GAGAL untuk Dokumen ID: ${doc.id}. Nama (name) bernilai null.")
                                failCount++
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception saat konversi dokumen ${doc.id} ke Checkpoint.", e)
                            failCount++
                        }
                    }

                    Log.i(TAG, "Selesai memuat. Berhasil: $successCount, Gagal: $failCount. Total ditampilkan: ${checkpointList.size}")
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Gagal memuat checkpoints dari Firestore!", exception)
                    Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } ?: run {
            Log.e(TAG, "Pengguna tidak terautentikasi (User is null). Tidak bisa memuat data.")
        }
    }

    override fun onResume() {
        super.onResume()
        loadCheckpoints()
    }

    // 3. Implementasi fungsi onMapClick dari interface
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