package com.example.firebase_tut

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase_tut.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var collegeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val nameEditText: EditText = findViewById(R.id.edit_name)
        val emailEditText: EditText = findViewById(R.id.edit_email)
        val passwordEditText: EditText = findViewById(R.id.edit_password)
        val reenterPasswordEditText: EditText = findViewById(R.id.edit_reenter_password)
        val registerButton: Button = findViewById(R.id.button_register)
        val loginTextView: TextView = findViewById(R.id.text_login)
        collegeSpinner = findViewById(R.id.spinner_college)

        loadColleges()

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val reenterPassword = reenterPasswordEditText.text.toString().trim()
            val collegeId = collegeSpinner.selectedItem as? String ?: ""

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty() || collegeId.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != reenterPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password, collegeId)
        }

        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun loadColleges() {
        Log.d("RegistrationActivity", "Loading colleges from Firebase...")
        database.child("colleges").get().addOnSuccessListener { dataSnapshot ->
            Log.d("RegistrationActivity", "Colleges loaded successfully.")
            val collegeList = mutableListOf<String>()
            for (snapshot in dataSnapshot.children) {
                val collegeName = snapshot.key
                if (collegeName != null) {
                    Log.d("RegistrationActivity", "College found: $collegeName")
                    collegeList.add(collegeName)
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, collegeList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            collegeSpinner.adapter = adapter
        }.addOnFailureListener { exception ->
            Log.e("RegistrationActivity", "Failed to load colleges.", exception)
            Toast.makeText(this, "Failed to load colleges.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(name: String, email: String, password: String, collegeId: String) {
        Log.d("RegistrationActivity", "Registering user with email: $email")

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Create User object
                    val user = User(
                        userId = userId,
                        userName = name,
                        email = email,
                        role = "student",
                        approved = false,
                        collegeId = collegeId
                    )

                    // Save user in the 'users' node
                    database.child("users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            Log.d("RegistrationActivity", "User saved to 'users' node successfully.")

                            // Also save user under the college node
                            database.child("colleges").child(collegeId).child("users").child(userId)
                                .setValue(true)
                                .addOnSuccessListener {
                                    Log.d("RegistrationActivity", "User registered successfully.")
                                    Toast.makeText(this, "Registration Successful. Awaiting approval.", Toast.LENGTH_SHORT).show()
                                    // Navigate to the login activity
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("RegistrationActivity", "Failed to save user under college node.", exception)
                                    Toast.makeText(this, "Failed to save user under college node.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RegistrationActivity", "Failed to save user to 'users' node.", exception)
                            Toast.makeText(this, "Failed to save user to 'users' node.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("RegistrationActivity", "Registration failed: ${task.exception?.message}", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
