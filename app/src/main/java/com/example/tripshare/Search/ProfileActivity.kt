package com.example.tripshare.Search

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Account.Photo
import com.example.tripshare.Account.PhotosAdapter
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var userNameText: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var pubNumTextView: TextView
    private lateinit var adapter: PhotosAdapter
    private val photosList = mutableListOf<Photo>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar vistas
        userNameText = findViewById(R.id.username)
        profileImageView = findViewById(R.id.profile_image)
        recyclerView = findViewById(R.id.recyclerViewPhotos)
        pubNumTextView = findViewById(R.id.pubNum)

        // Obtener el nombre del usuario desde el Intent
        val userName = intent.getStringExtra("USER_NAME") ?: ""

        userNameText.text = userName
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = PhotosAdapter("")
        recyclerView.adapter = adapter


        // Cargar la información del usuario
        loadUserInfo(userName)
    }

    private fun loadUserInfo(userName: String) {
        val formattedUserName = userName.trim().lowercase() // Convertir a minúsculas y quitar espacios extra

        db.collection("users")
            .whereEqualTo("name", formattedUserName)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val userUid = document.id
                    val userImageUri = document.getString("imageUri") ?: ""

                    // Mostrar la imagen de perfil
                    if (userImageUri.isNotEmpty()) {
                        Picasso.get()
                            .load(userImageUri)
                            .placeholder(R.drawable.ic_fragment_account)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_fragment_account)
                    }

                    // Cargar las fotos del usuario
                    loadUserPhotos(userUid)
                } else {
                    Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadUserPhotos(userUid: String) {
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                val photosList = mutableListOf<Photo>()

                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: continue
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""

                    Log.d("FirestoreDebug", "Imagen cargada: $imageUrl")

                    photosList.add(Photo(imageUrl, description, location))
                }

                adapter.updatePhotos(photosList)  // 🔹 Se actualizan las fotos en el adaptador correctamente
                updatePhotoCount(photosList.size)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updatePhotoCount(count: Int) {
        pubNumTextView.text = count.toString()
    }
}
