package com.example.firebase_tut

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import android.widget.Toast

class ResourcesFragment : Fragment() {

    private lateinit var resourceListView: ListView
    private lateinit var contributeResourceButton: View
    private val resourceIds = mutableListOf<String>()
    private val resourceNames = mutableListOf<String>()
    private lateinit var resourceAdapter: ArrayAdapter<String>

    companion object {
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(courseId: String): ResourcesFragment {
            val fragment = ResourcesFragment()
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
        val view = inflater.inflate(R.layout.fragment_resources, container, false)
        resourceListView = view.findViewById(R.id.resource_list_view)
        contributeResourceButton = view.findViewById(R.id.add_button)

        resourceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, resourceNames)
        resourceListView.adapter = resourceAdapter

        fetchResources()

        // Add listener to handle resource clicks
        resourceListView.setOnItemClickListener { _, _, position, _ ->
            val resourceId = resourceIds[position]
            val resourceDetailFragment = ResourceDetailFragment.newInstance(resourceId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, resourceDetailFragment)
                .addToBackStack(null)
                .commit()
        }

        contributeResourceButton.setOnClickListener {
            val contributeResourceFragment = ContributeResourceFragment.newInstance(arguments?.getString(ARG_COURSE_ID) ?: "")
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, contributeResourceFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun fetchResources() {
        val courseId = arguments?.getString(ARG_COURSE_ID) ?: return
        val database = FirebaseDatabase.getInstance().reference.child("courses").child(courseId).child("resources")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                resourceIds.clear()
                resourceNames.clear()

                var processedCount = 0
                val totalCount = snapshot.childrenCount

                snapshot.children.forEach { resourceSnapshot ->
                    val resourceId = resourceSnapshot.getValue(String::class.java)

                    if (resourceId != null) {
                        val resourceRef = FirebaseDatabase.getInstance().reference.child("resources").child(resourceId)
                        resourceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(resourceDetailSnapshot: DataSnapshot) {
                                val resourceName = resourceDetailSnapshot.child("resourceName").getValue(String::class.java)
                                val isApproved = resourceDetailSnapshot.child("approved").getValue(Boolean::class.java) ?: false

                                if (resourceName != null && isApproved) {
                                    resourceIds.add(resourceId)
                                    resourceNames.add(resourceName)
                                }

                                processedCount++

                                if (processedCount == totalCount.toInt()) {
                                    resourceAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ResourcesFragment", "Failed to load resource details: ${error.message}")
                                Toast.makeText(requireContext(), "Failed to load resource details: ${error.message}", Toast.LENGTH_LONG).show()
                                processedCount++
                                if (processedCount == totalCount.toInt()) {
                                    resourceAdapter.notifyDataSetChanged()
                                }
                            }
                        })
                    } else {
                        processedCount++
                        if (processedCount == totalCount.toInt()) {
                            resourceAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ResourcesFragment", "Failed to load resources: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load resources: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
