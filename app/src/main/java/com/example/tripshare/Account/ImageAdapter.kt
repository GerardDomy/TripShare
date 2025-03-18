package com.example.tripshare.Account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripshare.R

class ImageAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_post, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = photos[position]
        holder.bind(photo)
    }

    override fun getItemCount(): Int = photos.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)

        fun bind(photo: Photo) {
            Glide.with(itemView.context).load(photo.imageUrl).into(imageView)
            textDescription.text = photo.description
            textLocation.text = photo.location
        }
    }
}
