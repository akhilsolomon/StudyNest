package com.example.firebase_tut

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class CourseDetailFragment : Fragment() {

    private lateinit var showResourcesButton: Button
    private lateinit var showFeedbacksButton: Button

    companion object {
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(courseId: String): CourseDetailFragment {
            val fragment = CourseDetailFragment()
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
        val view = inflater.inflate(R.layout.fragment_course_detail, container, false)
        showResourcesButton = view.findViewById(R.id.button_show_resources)
        showFeedbacksButton = view.findViewById(R.id.button_show_feedbacks)

        showResourcesButton.setOnClickListener {
            val courseId = arguments?.getString(ARG_COURSE_ID) ?: return@setOnClickListener
            val resourcesFragment = ResourcesFragment.newInstance(courseId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, resourcesFragment)
                .addToBackStack(null)
                .commit()
        }

        showFeedbacksButton.setOnClickListener {
            val courseId = arguments?.getString(ARG_COURSE_ID) ?: return@setOnClickListener
            val feedbacksFragment = FeedbacksFragment.newInstance(courseId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, feedbacksFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}

