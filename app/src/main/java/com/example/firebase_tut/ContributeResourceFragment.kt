package com.example.firebase_tut

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ContributeResourceFragment : Fragment() {

    private lateinit var resourceNameEditText: EditText
    private lateinit var resourceDescriptionEditText: EditText
    private lateinit var resourceUrlEditText: EditText
    private lateinit var submitButton: LinearLayout
    private lateinit var database: DatabaseReference
    private var courseId: String? = null

    companion object {
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(courseId: String): ContributeResourceFragment {
            val fragment = ContributeResourceFragment()
            val args = Bundle()
            args.putString(ARG_COURSE_ID, courseId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contribute_resource, container, false)
        resourceNameEditText = view.findViewById(R.id.edit_text_resource_name)
        resourceDescriptionEditText = view.findViewById(R.id.edit_text_resource_description)
        resourceUrlEditText = view.findViewById(R.id.edit_text_resource_url)
        submitButton = view.findViewById(R.id.button_submit)

        database = FirebaseDatabase.getInstance().reference
        courseId = arguments?.getString(ARG_COURSE_ID)

        submitButton.setOnClickListener {
            courseId?.let {
                contributeResource(it)
            }
        }

        return view
    }

    private fun contributeResource(courseId: String) {
        val resourceName = resourceNameEditText.text.toString().trim()
        val resourceDescription = resourceDescriptionEditText.text.toString().trim()
        val resourceUrl = resourceUrlEditText.text.toString().trim()

        if (resourceName.isEmpty() || resourceDescription.isEmpty() || resourceUrl.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }

        val newResourceRef = database.child("resources").push()
        val resourceId = newResourceRef.key ?: return

        val resource = mapOf(
            "resourceId" to resourceId,
            "resourceName" to resourceName,
            "resourceDescription" to resourceDescription,
            "resourceUrl" to resourceUrl,
            "approved" to false,
            "courseId" to courseId
        )

        newResourceRef.setValue(resource)
            .addOnSuccessListener {
                updateCourseResources(courseId, resourceId)
            }
            .addOnFailureListener {
                showError("Failed to contribute resource.")
            }
    }

    private fun updateCourseResources(courseId: String, resourceId: String) {
        val courseResourcesRef = database.child("courses").child(courseId).child("resources")

        courseResourcesRef.push().setValue(resourceId)
            .addOnSuccessListener {
                showSuccess("Resource contributed successfully.")
                navigateBack() // Navigate back after success
            }
            .addOnFailureListener {
                showError("Failed to update course resources.")
            }
    }

    private fun navigateBack() {
        parentFragmentManager.popBackStack()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
