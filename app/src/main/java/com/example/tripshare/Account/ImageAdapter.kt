package com.example.tripshare.Account

import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ImageAdapter(
    private val photos: List<Photo>,
    private val userName: String,
    private val userImageUri: String
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_post, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = photos[position]
        holder.bind(photo, userName, userImageUri)
    }

    override fun getItemCount(): Int = photos.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.image_profile)
        private val profileName: TextView = itemView.findViewById(R.id.profile_username)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)
        private val buttonLike: ImageButton = itemView.findViewById(R.id.button_like)
        private val likeEffectView: ImageView = itemView.findViewById(R.id.like_effect_view)

        private var isLiked = false
        private lateinit var photoId: String
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
        }

        fun bind(photo: Photo, userName: String, userImageUri: String) {
            profileName.text = userName
            this.photoId = photo.id

            // Carga la imagen de perfil
            if (userImageUri.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(userImageUri)
                    .circleCrop()
                    .into(profileImage)
            }

            // Carga la imagen de la foto
            Glide.with(itemView.context)
                .load(photo.imageUrl)
                .into(imageView)

            // Asegúrate de asignar los valores correctamente a la descripción y la localización
            textDescription.text = photo.description
            textLocation.text = photo.location

            // Inicializamos el efecto de "like"
            likeEffectView.visibility = View.GONE
            checkIfLiked()
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
                    likeRef.set(mapOf("liked" to true))
                } else {
                    likeRef.delete()
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
    }
}



