package com.example.firebase_tut

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase_tut.model.User

class PendingUsersAdapter(
    private val context: Context,
    private val onApproveClicked: (User, Boolean) -> Unit
) : RecyclerView.Adapter<PendingUsersAdapter.PendingUserViewHolder>() {

    private val users = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingUserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_user, parent, false)
        return PendingUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingUserViewHolder, position: Int) {
        val user = users[position]
        holder.userNameTextView.text = user.userName
        holder.userEmailTextView.text = user.email
        holder.approveButton.setOnClickListener {
            onApproveClicked(user, true)
        }
        holder.disapproveButton.setOnClickListener {
            onApproveClicked(user, false)
        }
    }

    override fun getItemCount(): Int = users.size

    fun setUsers(usersList: List<User>) {
        users.clear()
        users.addAll(usersList)
        notifyDataSetChanged()
    }

    class PendingUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.text_user_name)
        val userEmailTextView: TextView = itemView.findViewById(R.id.text_user_email)
        val approveButton: Button = itemView.findViewById(R.id.button_approve)
        val disapproveButton: Button = itemView.findViewById(R.id.button_disapprove)
    }
}
