package com.example.tripshare.Account

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewImageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var photosList: MutableList<Photo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ImageAdapter(photosList)
        recyclerView.adapter = adapter

        loadPhotos()
    }

    private fun loadPhotos() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.get()
            .addOnSuccessListener { documents ->
                photosList.clear() // Limpiar lista antes de agregar nuevos datos
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""

                    if (imageUrl.isNotEmpty()) {
                        photosList.add(Photo(imageUrl, description, location))
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
            }
    }

}

