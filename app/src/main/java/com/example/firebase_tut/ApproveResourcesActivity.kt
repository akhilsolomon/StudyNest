package com.example.firebase_tut

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase_tut.model.Resource
import com.google.firebase.database.*

class ApproveResourcesActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var pendingResourcesAdapter: PendingResourcesAdapter
    private lateinit var collegeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_resources)

        collegeId = intent.getStringExtra("collegeId") ?: run {
            Toast.makeText(this, "No College ID provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        loadPendingResources()
    }

    private fun setupRecyclerView() {
        pendingResourcesAdapter = PendingResourcesAdapter(this) { resourceId, courseId ->
            val intent = Intent(this, ResourceDetailActivity::class.java)
            intent.putExtra("resourceId", resourceId)
            intent.putExtra("courseId", courseId)
            startActivity(intent)
        }

        val resourceRecyclerView: RecyclerView = findViewById(R.id.recycler_pending_resources)
        resourceRecyclerView.layoutManager = LinearLayoutManager(this)
        resourceRecyclerView.adapter = pendingResourcesAdapter
    }

    private fun loadPendingResources() {
        Log.d("ApproveResourcesActivity", "Loading pending resources for collegeId: $collegeId")

        database.child("colleges").child(collegeId).child("branches")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ApproveResourcesActivity", "Branches loaded: ${snapshot.childrenCount}")
                    val branchIds = mutableListOf<String>()
                    for (branchSnapshot in snapshot.children) {
                        val branchId = branchSnapshot.value as? String
                        if (branchId != null) {
                            branchIds.add(branchId)
                        }
                    }
                    // Load courses for all branches
                    for (branchId in branchIds) {
                        Log.d("ApproveResourcesActivity", "Loading courses for branchId: $branchId")
                        loadCoursesForBranch(branchId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApproveResourcesActivity", "Failed to load branches: ${error.message}")
                    Toast.makeText(this@ApproveResourcesActivity, "Failed to load branches: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadCoursesForBranch(branchId: String) {
        database.child("branches").child(branchId).child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ApproveResourcesActivity", "Courses loaded for branchId: $branchId, count: ${snapshot.childrenCount}")

                    if (snapshot.exists()) {
                        for (courseSnapshot in snapshot.children) {
                            val courseId = courseSnapshot.value as? String
                            if (courseId != null) {
                                Log.d("ApproveResourcesActivity", "Loading resources for courseId: $courseId")
                                loadResourcesForCourse(courseId)
                            }
                        }
                    } else {
                        Log.d("ApproveResourcesActivity", "No courses found for branchId: $branchId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApproveResourcesActivity", "Failed to load courses: ${error.message}")
                    Toast.makeText(this@ApproveResourcesActivity, "Failed to load courses: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadResourcesForCourse(courseId: String) {
        database.child("courses").child(courseId).child("resources")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ApproveResourcesActivity", "Resources loaded for courseId: $courseId, count: ${snapshot.childrenCount}")
                    val unapprovedResources = mutableListOf<Resource>()
                    val totalResources = snapshot.childrenCount
                    var resourcesProcessed = 0

                    if (snapshot.exists()) {
                        for (resourceSnapshot in snapshot.children) {
                            val resourceId = resourceSnapshot.value as? String
                            if (resourceId != null) {
                                database.child("resources").child(resourceId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(resourceSnapshot: DataSnapshot) {
                                        val resource = resourceSnapshot.getValue(Resource::class.java)
                                        if (resource != null && !resource.approved) {
                                            unapprovedResources.add(resource)
                                        }
                                        resourcesProcessed++

                                        // After processing all resources, update the adapter
                                        if (resourcesProcessed.toLong() == totalResources) {
                                            pendingResourcesAdapter.addResources(unapprovedResources)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("ApproveResourcesActivity", "Failed to load resource details: ${error.message}")
                                        Toast.makeText(this@ApproveResourcesActivity, "Failed to load resource details: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            } else {
                                resourcesProcessed++
                                // After processing all resources, update the adapter if no resource ID is found
                                if (resourcesProcessed.toLong() == totalResources) {
                                    pendingResourcesAdapter.addResources(unapprovedResources)
                                }
                            }
                        }
                    } else {
                        Log.d("ApproveResourcesActivity", "No resources found for courseId: $courseId")
                        // No resources for this course, update adapter with empty list
                        pendingResourcesAdapter.addResources(unapprovedResources)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApproveResourcesActivity", "Failed to load resources: ${error.message}")
                    Toast.makeText(this@ApproveResourcesActivity, "Failed to load resources: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
