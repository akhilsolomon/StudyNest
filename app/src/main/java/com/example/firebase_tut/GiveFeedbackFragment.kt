package com.example.firebase_tut

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GiveFeedbackFragment : Fragment() {

    private lateinit var feedbackEditText: EditText
    private lateinit var submitButton: LinearLayout // Changed from Button to LinearLayout
    private lateinit var database: DatabaseReference
    private var courseId: String? = null

    companion object {
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(courseId: String): GiveFeedbackFragment {
            val fragment = GiveFeedbackFragment()
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
        val view = inflater.inflate(R.layout.fragment_give_feedback, container, false)
        feedbackEditText = view.findViewById(R.id.enter_feedback)
        submitButton = view.findViewById(R.id.continue_button) // Updated to reference the LinearLayout

        database = FirebaseDatabase.getInstance().reference
        courseId = arguments?.getString(ARG_COURSE_ID)

        submitButton.setOnClickListener {
            courseId?.let {
                giveFeedback(it)
            }
        }

        return view
    }

    private fun giveFeedback(courseId: String) {
        val feedbackText = feedbackEditText.text.toString().trim()

        if (feedbackText.isEmpty()) {
            // Handle empty input case
            showError("Please enter feedback.")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val feedbackRef = database.child("feedback")
        val newFeedbackRef = feedbackRef.push()
        val feedbackId = newFeedbackRef.key ?: return

        val feedback = mapOf(
            "feedbackId" to feedbackId,
            "description" to feedbackText,
            "courseId" to courseId,
            "uploadedBy" to userId
        )

        newFeedbackRef.setValue(feedback)
            .addOnSuccessListener {
                // Update the course's feedback list
                updateCourseFeedback(courseId, feedbackId)
            }
            .addOnFailureListener {
                // Handle failure
                showError("Failed to submit feedback.")
            }
    }

    private fun updateCourseFeedback(courseId: String, feedbackId: String) {
        val courseFeedbackRef = database.child("courses").child(courseId).child("feedback")

        courseFeedbackRef.push().setValue(feedbackId)
            .addOnSuccessListener {
                showSuccess("Feedback submitted successfully.")
                // Redirect to the previous page after successful submission
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                showError("Failed to update course feedback.")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
