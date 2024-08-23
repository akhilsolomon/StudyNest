package com.example.firebase_tut

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Ensure you have a layout for SplashActivity

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI elements
        val loginButton: Button = findViewById(R.id.button_login)
        val registerButton: Button = findViewById(R.id.button_register)

        // Set button click listeners
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }

        // Check if a user is already logged in
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in, check their role
            val userId = currentUser.uid
            database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userRole = snapshot.child("role").getValue(String::class.java)
                    val isApproved = snapshot.child("approved").getValue(Boolean::class.java) ?: false

                    if (isApproved) {
                        // Redirect based on user role
                        if (userRole == "admin") {
                            startActivity(Intent(this@SplashActivity, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this@SplashActivity, UserMainActivity::class.java))
                        }
                        finish()
                    } else {
                        // Redirect to LoginActivity if not approved
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Toast.makeText(this@SplashActivity, "Error checking user role.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            })
        }
    }
}
