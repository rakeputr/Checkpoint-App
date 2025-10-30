package com.example.latihanfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditLocationActivity : AppCompatActivity() {

    // Kunci intent sama dengan DetailActivity
    companion object {
        const val EXTRA_CHECKPOINT_ID = "extra_checkpoint_id"
    }

    private val db = FirebaseFirestore.getInstance()
    private var checkpointId: String? = null

    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var tvCoordinates: TextView // Menggunakan TextView untuk menampilkan koordinat
    private lateinit var btnUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_location)


        etName = findViewById(R.id.etNameEdit)
        etDesc = findViewById(R.id.etDescEdit)
        tvCoordinates = findViewById(R.id.tvCoordinatesEdit)
        btnUpdate = findViewById(R.id.btnUpdate)

        checkpointId = intent.getStringExtra(EXTRA_CHECKPOINT_ID)

        if (checkpointId == null) {
            Toast.makeText(this, "ID Checkpoint tidak valid.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadExistingData(checkpointId!!)
        }

        btnUpdate.setOnClickListener {
            updateLocation()
        }
    }

    private fun loadExistingData(id: String) {
        db.collection("checkpoints").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val checkpoint = document.toObject(Checkpoint::class.java)

                    if (checkpoint != null) {
                        // Isi field dengan data lama
                        etName.setText(checkpoint.name)
                        etDesc.setText(checkpoint.description)

                        // nampilin koordinat, tp ga diupdate
                        val lat = checkpoint.latitude ?: 0.0
                        val lng = checkpoint.longitude ?: 0.0
                        tvCoordinates.text = "Lat: $lat, Lng: $lng"
                    }
                } else {
                    Toast.makeText(this, "Data tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data lama.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun updateLocation() {
        val id = checkpointId ?: return
        val name = etName.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama tempat harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        // yang mau di update
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "description" to desc
            // lokasi gabisa diubah
        )
0.0
        db.collection("checkpoints").document(id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Checkpoint berhasil diperbarui! ✅", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui: ${it.message} ❌", Toast.LENGTH_SHORT).show()
            }
    }
}