package com.jcaa.usersmanagement.mobile.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jcaa.usersmanagement.mobile.data.UserResponse
import com.jcaa.usersmanagement.mobile.databinding.ItemUserBinding

class UserAdapter(
    private val onClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val items = mutableListOf<UserResponse>()

    fun submit(users: List<UserResponse>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserResponse) {
            binding.nameLabel.text = user.name
            binding.emailLabel.text = user.email
            binding.metaLabel.text = "${user.role} · ${user.status}"
            binding.root.setOnClickListener { onClick(user) }
        }
    }
}
