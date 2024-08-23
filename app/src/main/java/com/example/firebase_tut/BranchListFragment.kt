package com.example.firebase_tut

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BranchListFragment : Fragment() {

    private lateinit var branchListView: ListView
    private val branches = mutableListOf<String>() // This will now hold branch names
    private val branchIds = mutableListOf<String>() // This will hold branch IDs
    private lateinit var branchAdapter: ArrayAdapter<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_branch_list, container, false)
        branchListView = view.findViewById(R.id.branch_list_view)

        branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, branches)
        branchListView.adapter = branchAdapter

        fetchBranches()

        branchListView.setOnItemClickListener { _, _, position, _ ->
            val branchId = branchIds[position]
            (activity as? UserMainActivity)?.openCourseListFragment(branchId)
        }

        return view
    }

    private fun fetchBranches() {
        val database = FirebaseDatabase.getInstance().reference.child("branches")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                branches.clear()
                branchIds.clear()
                snapshot.children.forEach {
                    val branchId = it.key ?: return
                    val branchName = it.child("branchName").getValue(String::class.java) ?: branchId
                    branches.add(branchName)
                    branchIds.add(branchId)
                }
                branchAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error and show a Toast message
                Toast.makeText(requireContext(), "Failed to load branches: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
