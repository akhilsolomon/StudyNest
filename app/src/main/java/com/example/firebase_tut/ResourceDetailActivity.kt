package com.example.firebase_tut

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase_tut.model.Resource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ResourceDetailActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var resourceId: String
    private lateinit var courseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource_detail)

        resourceId = intent.getStringExtra("resourceId") ?: run {
            Toast.makeText(this, "No Resource ID provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        courseId = intent.getStringExtra("courseId") ?: run {
            Toast.makeText(this, "No Course ID provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        database = FirebaseDatabase.getInstance().reference

        val resourceNameTextView: TextView = findViewById(R.id.text_resource_name)
        val resourceUrlTextView: TextView = findViewById(R.id.text_resource_url)
        val descriptionTextView: TextView = findViewById(R.id.text_resource_description)
        val approveButton: Button = findViewById(R.id.button_approve)
        val disapproveButton: Button = findViewById(R.id.button_disapprove)

        // Load resource details from the database
        loadResourceDetails(resourceNameTextView, resourceUrlTextView, descriptionTextView)

        approveButton.setOnClickListener {
            approveResource()
        }

        disapproveButton.setOnClickListener {
            disapproveResource()
        }
    }

    private fun loadResourceDetails(
        resourceNameTextView: TextView,
        resourceUrlTextView: TextView,
        descriptionTextView: TextView
    ) {
        Log.d("ResourceDetailActivity", "Loading details for resourceId: $resourceId, courseId: $courseId")

        database.child("resources").child(resourceId)
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val resource = dataSnapshot.getValue(Resource::class.java)
                if (resource != null) {
                    resourceNameTextView.text = resource.resourceName
                    resourceUrlTextView.text = resource.resourceUrl
                    descriptionTextView.text = resource.description
                } else {
                    Toast.makeText(this, "Resource not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Log.e("ResourceDetailActivity", "Failed to load resource details: ${it.message}")
                Toast.makeText(this, "Failed to load resource details.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveResource() {
        Log.d("ResourceDetailActivity", "Approving resourceId: $resourceId")

        database.child("resources").child(resourceId)
            .child("approved").setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "Resource approved successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Log.e("ResourceDetailActivity", "Failed to approve resource: ${it.message}")
                Toast.makeText(this, "Failed to approve resource.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun disapproveResource() {
        Log.d("ResourceDetailActivity", "Disapproving resourceId: $resourceId")

        database.child("resources").child(resourceId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Resource disapproved and removed successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Log.e("ResourceDetailActivity", "Failed to disapprove resource: ${it.message}")
                Toast.makeText(this, "Failed to disapprove resource.", Toast.LENGTH_SHORT).show()
            }
    }
}
