package com.example.firebase_tut
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.firebase_tut.model.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class ResourceDetailFragment : Fragment() {

    private lateinit var resourceNameTextView: TextView
    private lateinit var resourceDescriptionTextView: TextView
    private lateinit var resourceUrlTextView: TextView

    companion object {
        private const val ARG_RESOURCE_ID = "resource_id"

        fun newInstance(resourceId: String): ResourceDetailFragment {
            val fragment = ResourceDetailFragment()
            val args = Bundle()
            args.putString(ARG_RESOURCE_ID, resourceId)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_detail, container, false)
        resourceNameTextView = view.findViewById(R.id.text_view_resource_name)
        resourceDescriptionTextView = view.findViewById(R.id.text_view_resource_description)
        resourceUrlTextView = view.findViewById(R.id.text_view_resource_url)

        val resourceId = arguments?.getString(ARG_RESOURCE_ID) ?: return view

        loadResourceDetails(resourceId)

        return view
    }

    private fun loadResourceDetails(resourceId: String) {
        FirebaseDatabase.getInstance().reference.child("resources").child(resourceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val resource = snapshot.getValue(Resource::class.java)
                        resource?.let {
                            resourceNameTextView.text = it.resourceName
                            resourceDescriptionTextView.text = it.description
                            resourceUrlTextView.text = it.resourceUrl
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}

