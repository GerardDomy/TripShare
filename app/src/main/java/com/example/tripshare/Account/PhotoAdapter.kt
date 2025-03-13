package com.example.tripshare.Account

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R

class PhotosAdapter(private val photosList: List<Uri>) : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    var onImageClickListener: ((Uri) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUri = photosList[position]
        holder.imageView.setImageURI(photoUri)

        holder.imageView.setOnClickListener {
            onImageClickListener?.invoke(photoUri)
        }
    }

    override fun getItemCount() = photosList.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
    }
}


