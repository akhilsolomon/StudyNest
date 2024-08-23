package com.example.firebase_tut

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase_tut.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var collegeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val approveUsersButton: Button = findViewById(R.id.button_approve_users)
        val approveResourcesButton: Button = findViewById(R.id.button_approve_resources)
        val logoutButton: Button = findViewById(R.id.button_logout)
        val adminUsername: TextView = findViewById(R.id.admin_username)
        val adminEmail: TextView = findViewById(R.id.admin_email)

        auth.currentUser?.uid?.let { adminId ->
            database.child("users").child(adminId).get().addOnSuccessListener { dataSnapshot ->
                val adminUser = dataSnapshot.getValue(User::class.java)
                if (adminUser != null && adminUser.role == "admin") {
                    collegeId = adminUser.collegeId
                    adminUsername.text = adminUser.userName
                    adminEmail.text = adminUser.email

                    approveUsersButton.setOnClickListener {
                        val intent = Intent(this, ApproveUsersActivity::class.java)
                        intent.putExtra("collegeId", collegeId)
                        startActivity(intent)
                    }

                    approveResourcesButton.setOnClickListener {
                        val intent = Intent(this, ApproveResourcesActivity::class.java)
                        intent.putExtra("collegeId", collegeId)
                        startActivity(intent)
                    }

                    logoutButton.setOnClickListener {
                        auth.signOut()
                        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Not authorized", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch admin data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
