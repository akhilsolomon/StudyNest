package com.example.firebase_tut

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class FeedbacksFragment : Fragment() {

    private lateinit var feedbackListView: ListView
    private lateinit var giveFeedbackButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private val feedbackDescriptions = mutableListOf<String>()
    private lateinit var feedbackAdapter: ArrayAdapter<String>
    private val userNameCache = mutableMapOf<String, String>() // Cache for user names

    companion object {
        private const val ARG_COURSE_ID = "course_id"
        private const val TAG = "FeedbacksFragment"

        fun newInstance(courseId: String): FeedbacksFragment {
            val fragment = FeedbacksFragment()
            val args = Bundle()
            args.putString(ARG_COURSE_ID, courseId)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedbacks, container, false)
        feedbackListView = view.findViewById(R.id.feedback_list_view)
        giveFeedbackButton = view.findViewById(R.id.button_give_feedback)
        progressBar = view.findViewById(R.id.progress_bar)
        errorTextView = view.findViewById(R.id.error_text_view)

        feedbackAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, feedbackDescriptions)
        feedbackListView.adapter = feedbackAdapter

        Log.d(TAG, "FeedbacksFragment view created, starting to fetch feedbacks.")
        fetchFeedbacks()

        feedbackListView.setOnItemClickListener { _, _, position, _ ->
            val feedbackDetail = feedbackDescriptions[position]
            Log.d(TAG, "Feedback item clicked: $feedbackDetail")
            // Show feedback details if needed
        }

        giveFeedbackButton.setOnClickListener {
            val courseId = arguments?.getString(ARG_COURSE_ID) ?: ""
            Log.d(TAG, "Give Feedback button clicked for course: $courseId")
            val giveFeedbackFragment = GiveFeedbackFragment.newInstance(courseId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, giveFeedbackFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun fetchFeedbacks() {
        progressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        feedbackListView.visibility = View.GONE

        val courseId = arguments?.getString(ARG_COURSE_ID) ?: run {
            Log.e(TAG, "Course ID is null or empty.")
            progressBar.visibility = View.GONE
            errorTextView.text = "Invalid course ID."
            errorTextView.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "Fetching feedbacks for course ID: $courseId")
        val database = FirebaseDatabase.getInstance().reference.child("courses").child(courseId).child("feedback")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Data snapshot received: ${snapshot.childrenCount} items")
                feedbackDescriptions.clear()
                if (snapshot.exists()) {
                    var processedCount = 0
                    val totalCount = snapshot.childrenCount

                    snapshot.children.forEach { feedbackSnapshot ->
                        val feedbackId = feedbackSnapshot.getValue(String::class.java)

                        if (feedbackId != null) {
                            val feedbackRef = FirebaseDatabase.getInstance().reference.child("feedback").child(feedbackId)
                            feedbackRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(feedbackDetailSnapshot: DataSnapshot) {
                                    val description = feedbackDetailSnapshot.child("description").getValue(String::class.java)
                                    val uploadedById = feedbackDetailSnapshot.child("uploadedBy").getValue(String::class.java)

                                    if (description != null && uploadedById != null) {
                                        // Check cache first
                                        val uploadedByName = userNameCache[uploadedById]
                                        if (uploadedByName != null) {
                                            val feedbackDetail = "$description\nUploaded by: $uploadedByName"
                                            feedbackDescriptions.add(feedbackDetail)
                                        } else {
                                            // Retrieve user name from users node
                                            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uploadedById)
                                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                                    val userName = userSnapshot.child("userName").getValue(String::class.java)
                                                    if (userName != null) {
                                                        userNameCache[uploadedById] = userName
                                                        val feedbackDetail = "$description\nUploaded by: $userName"
                                                        feedbackDescriptions.add(feedbackDetail)
                                                    } else {
                                                        Log.w(TAG, "User name is missing for ID: $uploadedById")
                                                    }
                                                    processedCount++
                                                    if (processedCount == totalCount.toInt()) {
                                                        feedbackAdapter.notifyDataSetChanged()
                                                        feedbackListView.visibility = View.VISIBLE
                                                        progressBar.visibility = View.GONE
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.e(TAG, "Failed to load user details: ${error.message}")
                                                    processedCount++
                                                    if (processedCount == totalCount.toInt()) {
                                                        feedbackAdapter.notifyDataSetChanged()
                                                        feedbackListView.visibility = View.VISIBLE
                                                        progressBar.visibility = View.GONE
                                                    }
                                                    Toast.makeText(requireContext(), "Failed to load user details: ${error.message}", Toast.LENGTH_LONG).show()
                                                }
                                            })
                                        }
                                    } else {
                                        Log.w(TAG, "Feedback details are missing for ID: $feedbackId")
                                    }

                                    processedCount++
                                    if (processedCount == totalCount.toInt()) {
                                        feedbackAdapter.notifyDataSetChanged()
                                        feedbackListView.visibility = View.VISIBLE
                                        progressBar.visibility = View.GONE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(TAG, "Failed to load feedback details: ${error.message}")
                                    processedCount++
                                    if (processedCount == totalCount.toInt()) {
                                        feedbackAdapter.notifyDataSetChanged()
                                        feedbackListView.visibility = View.VISIBLE
                                        progressBar.visibility = View.GONE
                                    }
                                    Toast.makeText(requireContext(), "Failed to load feedback details: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                        } else {
                            processedCount++
                            if (processedCount == totalCount.toInt()) {
                                feedbackAdapter.notifyDataSetChanged()
                                feedbackListView.visibility = View.VISIBLE
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No feedback available for course ID: $courseId")
                    errorTextView.text = "No feedback available for this course."
                    errorTextView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load feedbacks: ${error.message}", error.toException())
                progressBar.visibility = View.GONE
                errorTextView.text = "Failed to load feedbacks: ${error.message}"
                errorTextView.visibility = View.VISIBLE
            }
        })
    }
}
