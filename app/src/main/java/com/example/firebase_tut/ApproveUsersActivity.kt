package com.example.firebase_tut

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase_tut.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ApproveUsersActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var pendingUsersAdapter: PendingUsersAdapter
    private lateinit var collegeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_users)

        collegeId = intent.getStringExtra("collegeId") ?: run {
            Toast.makeText(this, "No College ID provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        loadPendingUsers()
    }

    private fun setupRecyclerView() {
        pendingUsersAdapter = PendingUsersAdapter(this) { user, approve ->
            if (approve) {
                approveUser(user)
            } else {
                disapproveUser(user)
            }
        }

        val userRecyclerView: RecyclerView = findViewById(R.id.recycler_pending_users)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = pendingUsersAdapter
    }

    private fun loadPendingUsers() {
        database.child("users")
            .orderByChild("collegeId")
            .equalTo(collegeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pendingUsers = mutableListOf<User>()
                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if (user != null && user.approved == false) {
                            pendingUsers.add(user)
                        }
                    }
                    pendingUsersAdapter.setUsers(pendingUsers)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ApproveUsersActivity, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun approveUser(user: User) {
        database.child("users").child(user.userId)
            .child("approved").setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "User approved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to approve user.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun disapproveUser(user: User) {
        database.child("users").child(user.userId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "User disapproved and removed successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to disapprove user.", Toast.LENGTH_SHORT).show()
            }
    }
}
