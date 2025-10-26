package com.example.latihanfirebase

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    // Kunci Intent yang digunakan oleh HomeActivity
    companion object {
        const val EXTRA_CHECKPOINT_ID = "extra_checkpoint_id"
    }

    private val db = FirebaseFirestore.getInstance()
    private var activeCheckpointId: String? = null // Menyimpan ID dokumen yang sedang ditampilkan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Checkpoint" // Judul Statis

        val btnEdit: Button = findViewById(R.id.btnEdit)
        val btnDelete: Button = findViewById(R.id.btnDelete)

        // 1. Ambil ID dokumen dari Intent
        val checkpointId = intent.getStringExtra(EXTRA_CHECKPOINT_ID)

        if (checkpointId != null) {
            activeCheckpointId = checkpointId // Simpan ID
        } else {
            Toast.makeText(this, "ID Checkpoint hilang!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Setup Listener Tombol
        btnEdit.setOnClickListener {
            editCheckpoint()
        }
        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        // Pastikan ID tersedia, lalu muat data terbaru
        activeCheckpointId?.let { id ->
            loadCheckpointDetails(id)
        }
    }

    // Fungsi untuk memuat data detail dari Firestore berdasarkan ID
    private fun loadCheckpointDetails(id: String) {
        db.collection("checkpoints").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val checkpoint = document.toObject(Checkpoint::class.java)
                    if (checkpoint != null) {
                        displayCheckpoint(checkpoint)
                    } else {
                        Toast.makeText(this, "Gagal memproses data.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Dokumen tidak ditemukan.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data detail: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Fungsi untuk menampilkan data ke elemen UI
    private fun displayCheckpoint(checkpoint: Checkpoint) {
        val tvName: TextView = findViewById(R.id.tvDetailName)
        val tvDesc: TextView = findViewById(R.id.tvDetailDescription)
        val tvCoords: TextView = findViewById(R.id.tvDetailCoordinates)
        val tvTimestamp: TextView = findViewById(R.id.tvDetailTimestamp)

        tvName.text = checkpoint.name ?: "N/A"
        tvDesc.text = checkpoint.description ?: "-"
        tvCoords.text = "Lat: ${checkpoint.latitude}, Lng: ${checkpoint.longitude}"

        if (checkpoint.timestamp != null) {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
            tvTimestamp.text = dateFormat.format(checkpoint.timestamp)
        } else {
            tvTimestamp.text = "-"
        }
    }

    // --- FUNGSI HAPUS ---

    private fun showDeleteConfirmationDialog() {
        if (activeCheckpointId == null) return

        AlertDialog.Builder(this)
            .setTitle("Hapus Checkpoint")
            .setMessage("Anda yakin ingin menghapus lokasi ini secara permanen? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("HAPUS") { dialog, which ->
                deleteCheckpoint()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCheckpoint() {
        activeCheckpointId?.let { id ->
            db.collection("checkpoints").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Lokasi berhasil dihapus! ✅", Toast.LENGTH_SHORT).show()
                    // Kembali ke HomeActivity dan tutup DetailActivity
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // --- FUNGSI EDIT ---
    private fun editCheckpoint() {
        if (activeCheckpointId != null) {
            val intent = Intent(this, EditLocationActivity::class.java) // Ganti ke EditLocationActivity
            intent.putExtra(EXTRA_CHECKPOINT_ID, activeCheckpointId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "ID Checkpoint belum dimuat.", Toast.LENGTH_SHORT).show()
        }
    }

    // Mengaktifkan tombol kembali (panah di toolbar)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}