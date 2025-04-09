package com.example.tripshare.Search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val mContext: Context,
    private val mUsers: MutableList<User>,
    private val onUserClicked: (User) -> Unit,
    private val showDeleteButton: Boolean // Nuevo parámetro renombrado
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers[position]
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
            onUserClicked(user)
        }

        // Mostrar u ocultar el botón según el estado de búsqueda
        holder.deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE

        // Configurar el clic del botón solo si es visible
        if (showDeleteButton) {
            holder.deleteButton.setOnClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val userId = user.userId

                    // Eliminar usuario en Firebase
                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUserUid)
                        .collection("visitedUsers")
                        .document(userId)
                        .delete()
                        .addOnSuccessListener {
                            mUsers.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                mContext,
                                "Error al eliminar usuario: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int = mUsers.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameText: TextView = itemView.findViewById(R.id.username)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profile_image)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }
}

