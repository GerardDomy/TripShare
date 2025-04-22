package com.example.tripshare.Account

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewImageActivity : AppCompatActivity() {

    private var viewedUserUid: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var photosList: MutableList<Photo> = mutableListOf()
    private var userName: String = "Usuario" // Valores por defecto
    private var userImageUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        viewedUserUid = intent.getStringExtra("USER_ID")

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadUserInfo() // Cargar la información del usuario

    }

    private fun loadUserInfo() {
        val userUid = viewedUserUid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userUid).get()
            .addOnSuccessListener { document ->
                userName = document.getString("name") ?: "Usuario"
                userImageUri = document.getString("imageUri") ?: ""

                adapter = ImageAdapter(photosList, userName, userImageUri)
                recyclerView.adapter = adapter
                loadPhotos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPhotos() {
        val userUid = viewedUserUid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                photosList.clear()
                for (document in documents) {
                    val id = document.id
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""

                    if (imageUrl.isNotEmpty()) {
                        photosList.add(Photo(imageUrl, description, location, id))
                    }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
            }
    }
}

