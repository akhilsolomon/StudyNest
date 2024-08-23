package com.example.firebase_tut

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase_tut.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Check if the user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, check their status
            checkUserStatusAndRedirect()
            return
        }

        val emailEditText: EditText = findViewById(R.id.edit_email)
        val passwordEditText: EditText = findViewById(R.id.edit_password)
        val loginButton: Button = findViewById(R.id.button_login)
        val registerTextView: TextView = findViewById(R.id.text_register)
        val roleRadioGroup: RadioGroup = findViewById(R.id.radio_group_role)
        val forgotPasswordButton: TextView = findViewById(R.id.button_forgot_password)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val selectedRoleId = roleRadioGroup.checkedRadioButtonId
            val role = if (selectedRoleId == R.id.radio_admin) "admin" else "student"

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password, role)
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resetPassword(email)
        }
    }

    private fun loginUser(email: String, password: String, role: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    database.child("users").child(userId).get()
                        .addOnSuccessListener { dataSnapshot ->
                            val user = dataSnapshot.getValue(User::class.java)
                            if (user != null) {
                                if (user.approved) {
                                    Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()
                                    // Navigate to the correct interface based on role
                                    if (user.role == "admin") {
                                        startActivity(Intent(this, AdminActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, UserMainActivity::class.java))
                                    }
                                    finish()  // Finish LoginActivity so user can't go back to it
                                } else {
                                    Toast.makeText(this, "Your account is awaiting approval.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to retrieve user data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserStatusAndRedirect() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    if (user.approved) {
                        if (user.role == "admin") {
                            startActivity(Intent(this, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, UserMainActivity::class.java))
                        }
                        finish()  // Finish LoginActivity so user can't go back to it
                    } else {
                        Toast.makeText(this, "Your account is awaiting approval.", Toast.LENGTH_SHORT).show()
                        // Optionally log out the user or handle the case where the user is not approved
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve user data: ${it.message}", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }

    private fun resetPassword(email: String) {
        database.child("users").get()
            .addOnSuccessListener { dataSnapshot ->
                var userFound: User? = null
                for (childSnapshot in dataSnapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null && user.email == email) {
                        userFound = user
                        break
                    }
                }

                if (userFound != null && userFound.approved) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "User not approved or not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check user status: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
