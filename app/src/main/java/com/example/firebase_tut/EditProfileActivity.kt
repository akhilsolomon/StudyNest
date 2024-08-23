package com.example.firebase_tut

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var reEnterPasswordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nameEditText = findViewById(R.id.edit_text_name)
        passwordEditText = findViewById(R.id.edit_text_password)
        reEnterPasswordEditText = findViewById(R.id.edit_text_reenter_password)
        saveButton = findViewById(R.id.button_save)

        auth = FirebaseAuth.getInstance()

        saveButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun saveProfileChanges() {
        val user = auth.currentUser
        val name = nameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val reEnteredPassword = reEnterPasswordEditText.text.toString().trim()

        if (name.isNotEmpty()) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateDatabaseUserName(name)
                        Toast.makeText(this, "Name updated successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update name.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        if (password.isNotEmpty()) {
            if (password == reEnteredPassword) {
                user?.updatePassword(password)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDatabaseUserName(name: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseRef.child("userName").setValue(name)
            .addOnSuccessListener {
                Toast.makeText(this, "Database updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update database.", Toast.LENGTH_SHORT).show()
            }
    }
}
