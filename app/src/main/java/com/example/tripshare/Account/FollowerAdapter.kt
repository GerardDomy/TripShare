package com.example.tripshare.Account

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.example.tripshare.Search.ProfileActivity
import com.example.tripshare.Search.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FollowerAdapter(
    private val context: Context,
    private val users: List<User> // Lista de seguidores o seguidos
) : RecyclerView.Adapter<FollowerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userNameText.text = user.name

        if (user.imageUri.isNotEmpty()) {
            Picasso.get()
                .load(user.imageUri)
                .placeholder(R.drawable.ic_fragment_account)
                .into(holder.profileImageView)
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_fragment_account)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java).apply {
                putExtra("USER_NAME", user.name)
            }
            context.startActivity(intent) // Inicia la actividad del perfil
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameText: TextView = itemView.findViewById(R.id.username)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profile_image)
    }
}
