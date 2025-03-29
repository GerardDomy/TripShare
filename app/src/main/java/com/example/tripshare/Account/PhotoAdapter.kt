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
    private val db = FirebaseFirestore.getInstance()
    var onImageClickListener: ((String) -> Unit)? = null
    private val user = FirebaseAuth.getInstance().currentUser

    init {
        loadPhotosFromFirestore()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photosList[position]

        Log.d("PhotosAdapter", "Cargando imagen desde: ${photo.imageUrl}")

        if (!photo.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(photo.imageUrl) // URL válida
                .placeholder(R.drawable.logo) // Imagen temporal mientras se carga
                .error(R.drawable.rounded_button) // Imagen en caso de error
                .into(holder.imageView)
        } else {
            Log.e("PhotosAdapter", "URL de imagen vacía o nula")
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

    private fun loadPhotosFromFirestore() {
        val userUid = user?.uid ?: return
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                photosList.clear()
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: continue
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""

                    Log.d("FirestoreDebug", "Imagen cargada: $imageUrl")

                    val photo = Photo(imageUrl, description, location)  // Crear objeto correctamente
                    photosList.add(photo)
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreDebug", "Error al cargar imágenes", exception)
            }
    }
}




