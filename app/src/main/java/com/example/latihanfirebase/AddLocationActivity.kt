package com.example.latihanfirebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
class AddLocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnSave: Button

    private var currentLat: Double? = null
    private var currentLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        etName = findViewById(R.id.etName)
        etDesc = findViewById(R.id.etDesc)
        btnSave = findViewById(R.id.btnSave)

        getCurrentLocation()

        btnSave.setOnClickListener {
            saveLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                    Toast.makeText(this, "Lokasi didapatkan ✅", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLocation() {
        val name = etName.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (name.isEmpty() || currentLat == null || currentLng == null) {
            Toast.makeText(this, "Isi nama dan pastikan lokasi aktif", Toast.LENGTH_SHORT).show()
            return
        }

        val uuid = UUID.randomUUID()

        val newCheckpoint = Checkpoint(
            id = uuid.toString(),
            userId = user?.uid,
            name = name,
            description = desc,
            latitude = currentLat,
            longitude = currentLng
        )

        db.collection("checkpoints")
            .add(newCheckpoint)
            .addOnSuccessListener {
                // Tampilkan notifikasi berhasil
                Toast.makeText(this, "Lokasi disimpan ✅", Toast.LENGTH_SHORT).show()

                // Balik ke HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                // biar gak numpuk activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan lokasi ❌", Toast.LENGTH_SHORT).show()
            }
    }
}