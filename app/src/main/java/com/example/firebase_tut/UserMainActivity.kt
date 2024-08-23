package com.example.firebase_tut

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserMainActivity : AppCompatActivity() {

    private lateinit var leftPanel: LinearLayout
    private lateinit var toggleButton: Button
    private lateinit var contentFrame: FrameLayout
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button
    private var isPanelCollapsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        leftPanel = findViewById(R.id.left_panel)
        toggleButton = findViewById(R.id.toggle_button)
        contentFrame = findViewById(R.id.content_frame)
        userNameTextView = findViewById(R.id.user_name)
        userEmailTextView = findViewById(R.id.user_email)
        editProfileButton = findViewById(R.id.edit_profile_button)
        logoutButton = findViewById(R.id.logout_button)

        // Initialize fragments
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, BranchListFragment())
            .commit()

        // Set user details
        val user = FirebaseAuth.getInstance().currentUser
        userNameTextView.text = user?.displayName ?: "User"
        userEmailTextView.text = user?.email ?: "Email"

        // Toggle the left panel visibility
        toggleButton.setOnClickListener {
            toggleLeftPanel()
        }

        // Handle Edit Profile button click
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun toggleLeftPanel() {
        if (isPanelCollapsed) {
            leftPanel.visibility = View.VISIBLE
            toggleButton.text = ">"
        } else {
            leftPanel.visibility = View.GONE
            toggleButton.text = "<"
        }
        isPanelCollapsed = !isPanelCollapsed
    }


    fun openCourseListFragment(branchId: String) {
        val fragment = CourseListFragment.newInstance(branchId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun openResourcesFragment(courseId: String) {
        val fragment = ResourcesFragment.newInstance(courseId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun openFeedbacksFragment(courseId: String) {
        val fragment = FeedbacksFragment.newInstance(courseId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun openContributeResourceFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, ContributeResourceFragment())
            .addToBackStack(null)
            .commit()
    }

    fun openGiveFeedbackFragment(courseId: String) {
        val fragment = GiveFeedbackFragment.newInstance(courseId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .addToBackStack(null)
            .commit()
    }
}

