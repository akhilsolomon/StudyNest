package com.example.firebase_tut

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseListFragment : Fragment() {

    private lateinit var courseListView: ListView
    private lateinit var searchCourse: EditText
    private val courseNames = mutableListOf<String>() // This will now hold course names
    private val courseIds = mutableListOf<String>() // This will hold course IDs
    private lateinit var courseAdapter: ArrayAdapter<String>

    companion object {
        private const val ARG_BRANCH_ID = "branch_id"

        fun newInstance(branchId: String): CourseListFragment {
            val fragment = CourseListFragment()
            val args = Bundle()
            args.putString(ARG_BRANCH_ID, branchId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_list, container, false)
        courseListView = view.findViewById(R.id.course_list_view)
        searchCourse = view.findViewById(R.id.search_course)

        courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, courseNames)
        courseListView.adapter = courseAdapter

        fetchCourses()

        courseListView.setOnItemClickListener { _, _, position, _ ->
            val courseId = courseIds[position]
            val courseDetailFragment = CourseDetailFragment.newInstance(courseId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, courseDetailFragment)
                .addToBackStack(null)
                .commit()
        }

        searchCourse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                courseAdapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun fetchCourses() {
        val branchId = arguments?.getString(ARG_BRANCH_ID) ?: return
        val database = FirebaseDatabase.getInstance().reference.child("branches").child(branchId).child("courses")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("CourseListFragment", "Fetching courses for branch: $branchId")

                courseNames.clear()
                courseIds.clear()

                // Iterate through each child in the snapshot
                snapshot.children.forEach { courseSnapshot ->
                    val courseId = courseSnapshot.getValue(String::class.java)

                    // Access the course details using the courseId
                    if (courseId != null) {
                        val courseRef = FirebaseDatabase.getInstance().reference.child("courses").child(courseId)
                        courseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(courseDetailSnapshot: DataSnapshot) {
                                val courseName = courseDetailSnapshot.child("courseName").getValue(String::class.java)

                                // Log the entire snapshot and the individual course data
                                Log.d("CourseListFragment", "Course Snapshot Data: ${courseDetailSnapshot.value}")
                                Log.d("CourseListFragment", "Fetched courseId: $courseId")
                                Log.d("CourseListFragment", "Fetched courseName: $courseName")

                                if (courseId != null && courseName != null) {
                                    courseNames.add(courseName)
                                    courseIds.add(courseId)
                                } else {
                                    Log.w("CourseListFragment", "CourseId or CourseName is null. CourseId: $courseId, CourseName: $courseName")
                                }

                                // Notify adapter of data changes once all courses are processed
                                if (courseIds.size.toLong() == snapshot.childrenCount) {
                                    Log.d("CourseListFragment", "Course names: $courseNames")
                                    Log.d("CourseListFragment", "Course IDs: $courseIds")
                                    courseAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("CourseListFragment", "Failed to load course details: ${error.message}")
                                Toast.makeText(requireContext(), "Failed to load course details: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CourseListFragment", "Failed to load courses: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load courses: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

}
