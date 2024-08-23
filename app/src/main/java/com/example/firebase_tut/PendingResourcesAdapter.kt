package com.example.firebase_tut

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase_tut.model.Resource

class PendingResourcesAdapter(
    private val context: Context,
    private val onResourceClicked: (String, String) -> Unit // Function to handle resource click
) : RecyclerView.Adapter<PendingResourcesAdapter.PendingResourceViewHolder>() {

    private val resources = mutableListOf<Resource>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingResourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_resource, parent, false)
        return PendingResourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingResourceViewHolder, position: Int) {
        val resource = resources[position]
        holder.bind(resource)
    }

    override fun getItemCount(): Int = resources.size

    fun addResources(resourceList: List<Resource>) {
        Log.d("PendingResourcesAdapter", "Adding ${resourceList.size} resources to adapter")
        resources.addAll(resourceList) // Append new resources
        notifyDataSetChanged()
    }

    inner class PendingResourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resourceNameTextView: TextView = itemView.findViewById(R.id.text_resource_name)

        fun bind(resource: Resource) {
            resourceNameTextView.text = resource.resourceName
            itemView.setOnClickListener {
                onResourceClicked(resource.resourceId, resource.courseId)
            }
        }
    }
}
