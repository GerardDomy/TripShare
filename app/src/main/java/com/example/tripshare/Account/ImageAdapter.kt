package com.example.tripshare.Account

import android.app.Activity
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ImageAdapter(
    private val photos: MutableList<Photo>,
    private val userName: String,
    private val userImageUri: String,
    private val currentUserUid: String,
    private val viewedUserUid: String
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_post, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = photos[position]
        holder.bind(photo, userName, currentUserUid == viewedUserUid)
    }

    override fun getItemCount(): Int = photos.size

    fun removePhotoAt(position: Int) {
        photos.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.image_profile)
        private val profileName: TextView = itemView.findViewById(R.id.profile_username)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val likeEffectView: ImageView = itemView.findViewById(R.id.like_effect_view)
        private val buttonOptions: ImageButton = itemView.findViewById(R.id.button_options)
        private val likeCountText: TextView = itemView.findViewById(R.id.text_like_count)

        private var isLiked = false
        private lateinit var photoId: String
        private lateinit var photoUrl: String
        private val userUid = FirebaseAuth.getInstance().currentUser?.uid
        private val db = FirebaseFirestore.getInstance()

        init {
            val gestureDetector = GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    setLike()
                    showLikeEffect()
                    return true
                }
            })

            imageView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }

            buttonLike.setOnClickListener {
                toggleLike()
            }

            buttonOptions.setOnClickListener {
                showOptionsMenu(it)
            }
        }

        fun bind(photo: Photo, userName: String,isCurrentUser: Boolean) {
            profileName.text = userName
            this.photoId = photo.id
            this.photoUrl = photo.imageUrl

            buttonOptions.visibility = if (isCurrentUser) View.VISIBLE else View.GONE
            // Cargar imagen de perfil del usuario visto (viewedUserUid)
            val userUid = viewedUserUid  // Utiliza el UID del usuario que estás viendo (no el usuario actual)
            if (userUid != null) {
                db.collection("users").document(userUid)
                    .get()
                    .addOnSuccessListener { document ->
                        val profileImageUrl = document.getString("imageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(itemView.context)
                                .load(profileImageUrl)  // Cargar la URL de la imagen de perfil del usuario visto
                                .circleCrop()  // Asegúrate de que la imagen sea redonda
                                .placeholder(R.drawable.ic_fragment_account)  // Placeholder por si no se encuentra
                                .into(profileImage)
                        } else {
                            // Si el usuario no tiene imagen de perfil, usar la imagen por defecto
                            profileImage.setImageResource(R.drawable.ic_fragment_account)
                        }
                    }
                    .addOnFailureListener {
                        // Si ocurre un error, usar la imagen por defecto
                        profileImage.setImageResource(R.drawable.ic_fragment_account)
                    }
            }

            // Resto del código para cargar la imagen, descripción, ubicación, likes, etc.
            Glide.with(itemView.context)
                .load(photo.imageUrl)
                .into(imageView)

            textDescription.text = photo.description
            textLocation.text = photo.location

            likeEffectView.visibility = View.GONE
            checkIfLiked()
            loadLikeCount()
        }



        private fun loadLikeCount() {
            db.collection("likes").document(photoId).collection("users")
                .get()
                .addOnSuccessListener { documents ->
                    val likeCount = documents.size()
                    likeCountText.text = likeCount.toString()
                }
        }

        private fun toggleLike() {
            isLiked = !isLiked
            updateLikeIcon()
            saveLikeStatus()
        }

        private fun setLike() {
            if (!isLiked) {
                isLiked = true
                updateLikeIcon()
                showLikeEffect()
                saveLikeStatus()
            }
        }

        private fun updateLikeIcon() {
            buttonLike.setImageResource(if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        }

        private fun showLikeEffect() {
            likeEffectView.setImageResource(R.drawable.ic_favorite_view)
            likeEffectView.visibility = View.VISIBLE

            val fadeInOut = AlphaAnimation(1f, 0f).apply {
                duration = 2000
                interpolator = DecelerateInterpolator()
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        likeEffectView.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }

            likeEffectView.startAnimation(fadeInOut)
        }

        private fun saveLikeStatus() {
            userUid?.let { uid ->
                val likeRef = db.collection("likes").document(photoId).collection("users").document(uid)
                if (isLiked) {
                    likeRef.set(mapOf("liked" to true)).addOnSuccessListener {
                        loadLikeCount()
                    }
                } else {
                    likeRef.delete().addOnSuccessListener {
                        loadLikeCount()
                    }
                }
            }
        }

        private fun checkIfLiked() {
            userUid?.let { uid ->
                val likeRef = db.collection("likes").document(photoId).collection("users").document(uid)
                likeRef.get().addOnSuccessListener { document ->
                    isLiked = document.exists()
                    updateLikeIcon()
                }
            }
        }

        private fun showOptionsMenu(view: View) {
            val popupMenu = PopupMenu(itemView.context, view)
            popupMenu.menuInflater.inflate(R.menu.options_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_description -> {
                        showEditDescriptionDialog()
                        true
                    }
                    R.id.edit_location -> {
                        showEditLocationDialog()
                        true
                    }
                    R.id.delete_photo -> {
                        deletePhoto()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        private fun showEditDescriptionDialog() {
            val builder = AlertDialog.Builder(itemView.context)
            val input = EditText(itemView.context).apply {
                setText(textDescription.text.toString())
                hint = "Nueva descripción"
            }

            builder.setTitle("Editar descripción")
                .setView(input)
                .setPositiveButton("Guardar") { dialog, _ ->
                    val newDescription = input.text.toString()
                    updateDescriptionInDatabase(newDescription)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        private fun showEditLocationDialog() {
            val builder = AlertDialog.Builder(itemView.context)
            val input = EditText(itemView.context).apply {
                setText(textLocation.text.toString())
                hint = "Nueva localización"
            }

            builder.setTitle("Editar localización")
                .setView(input)
                .setPositiveButton("Guardar") { dialog, _ ->
                    val newLocation = input.text.toString()
                    updateLocationInDatabase(newLocation)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        private fun updateDescriptionInDatabase(newDescription: String) {
            val photoRef = db.collection("users").document(userUid!!).collection("photos").document(photoId)
            photoRef.update("description", newDescription)
                .addOnSuccessListener {
                    textDescription.text = newDescription
                    Toast.makeText(itemView.context, "Descripción actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "Error al actualizar la descripción", Toast.LENGTH_SHORT).show()
                }
        }

        private fun updateLocationInDatabase(newLocation: String) {
            val photoRef = db.collection("users").document(userUid!!).collection("photos").document(photoId)
            photoRef.update("location", newLocation)
                .addOnSuccessListener {
                    textLocation.text = newLocation
                    Toast.makeText(itemView.context, "Localización actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "Error al actualizar la localización", Toast.LENGTH_SHORT).show()
                }
        }

        private fun deletePhoto() {
            val photoRef = db.collection("users").document(userUid!!).collection("photos").document(photoId)
            val likesRef = db.collection("likes").document(photoId).collection("users")

            // Borrar "likes"
            likesRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }

            // Borrar imagen del Storage
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl)
            storageRef.delete().addOnSuccessListener {
                // Borrar documento de Firestore
                photoRef.delete().addOnSuccessListener {
                    Toast.makeText(itemView.context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                    (itemView.context as? Activity)?.runOnUiThread {
                        removePhotoAt(adapterPosition)
                    }
                }.addOnFailureListener {
                    Toast.makeText(itemView.context, "Error al eliminar la foto", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(itemView.context, "Error al borrar imagen del Storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}





