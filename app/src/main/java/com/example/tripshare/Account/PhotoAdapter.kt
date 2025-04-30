package com.example.tripshare.Account

import android.util.Log
import com.example.tripshare.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore


class PhotosAdapter(private val userUid: String) : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    private var photosList: MutableList<Photo> = mutableListOf()
    var onImageClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photosList[position]

        Log.d("PhotosAdapter", "Cargando imagen desde: ${photo.imageUrl}")

        if (!photo.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(photo.imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.rounded_button)
                .into(holder.imageView)
        }

        holder.itemView.setOnClickListener {
            onImageClickListener?.invoke(photo.imageUrl)
        }
    }

    override fun getItemCount(): Int = photosList.size

    fun updatePhotos(newPhotos: List<Photo>) {
        photosList.clear()
        photosList.addAll(newPhotos)
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
    }
}





