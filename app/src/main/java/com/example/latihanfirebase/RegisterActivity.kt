package com.example.latihanfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var edit_email: EditText
    private lateinit var edit_password: EditText
    private lateinit var edit_confirm: EditText
    private lateinit var btn_register: Button
    private lateinit var tv_login: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        edit_email = findViewById(R.id.edit_register_email)
        edit_password = findViewById(R.id.edit_register_password)
        edit_confirm = findViewById(R.id.edit_register_confirm)
        btn_register = findViewById(R.id.btn_register)
        tv_login = findViewById(R.id.tv_cancel)

        btn_register.setOnClickListener {
            register()
        }

        tv_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun RegisterActivity.register() {

        var email = edit_email.text.toString()
        var password = edit_password.text.toString()
        var confirm = edit_confirm.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if(password != confirm) {
            Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("REGISTER SUCCESS", "createUserWithEmail:success")
                    val user = auth.currentUser

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    val exception = task.exception
                    if (exception != null) {
                        if (exception is FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "Password terlalu lemah", Toast.LENGTH_SHORT).show()
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                        } else if (exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Authentication failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Log.w("REGISTER FAILED", "createUserWithEmail:failure", task.exception)

                }
            }
    }
}


