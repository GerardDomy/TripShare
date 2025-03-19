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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var photosList: MutableList<Photo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "Usuario"
                    val imageUri = document.getString("imageUri") ?: ""

                    adapter = ImageAdapter(photosList, name, imageUri)
                    recyclerView.adapter = adapter
                    loadPhotos()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadPhotos() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                photosList.clear()
                for (document in documents) {
                    val id = document.id // Obtenemos el ID del documento
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val description = document.getString("description")
                        ?: "" // Asegúrate de que "description" esté bien guardado
                    val location = document.getString("location")
                        ?: "" // Asegúrate de que "location" esté bien guardado

                    if (imageUrl.isNotEmpty()) {
                        // Aquí estamos añadiendo el objeto Photo con los valores correctos
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

