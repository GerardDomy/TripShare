package com.example.tripshare.Account

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.example.tripshare.Search.User
import com.example.tripshare.Search.UserAdapter
import com.google.firebase.firestore.FirebaseFirestore

class FollowersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var isFollowersList: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followers)

        // Inicializamos el RecyclerView y el adapter
        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(this, userList) // Usamos el UserAdapter
        recyclerView.adapter = adapter

        // Recuperamos el nombre y el ID del usuario desde el Intent
        val userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
        userId = intent.getStringExtra("USER_ID")
        isFollowersList = intent.getBooleanExtra("IS_FOLLOWERS", true)

        // Configuramos el título
        val title = findViewById<TextView>(R.id.title)
        title.text = if (isFollowersList) "Seguidores de $userName" else "Seguidos por $userName"

        // Cargamos los usuarios
        loadFollowersAndFollowing(isFollowersList)
    }

    private fun loadFollowersAndFollowing(isFollowers: Boolean) {
        val userUid = userId ?: return // Usamos el ID del usuario que se pasó a la Activity

        // Seleccionamos la colección adecuada (seguidores o seguidos)
        val collection = if (isFollowers) {
            db.collection("users").document(userUid).collection("followers")
        } else {
            db.collection("users").document(userUid).collection("following")
        }

        collection.get().addOnSuccessListener { documents ->
            val users = mutableListOf<User>()

            // Recorremos todos los documentos de la colección (seguidores o seguidos)
            for (document in documents) {
                val userId = document.id // Supongo que el ID del documento es el UID del usuario

                db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                    // Cargamos la información del usuario (nombre y URI de la imagen)
                    val user = User(
                        name = userDoc.getString("name") ?: "Unknown",
                        imageUri = userDoc.getString("imageUri") ?: ""
                    )
                    users.add(user)

                    // Cuando se hayan cargado todos los usuarios, actualizamos el RecyclerView
                    if (users.size == documents.size()) {
                        val adapter = FollowerAdapter(this@FollowersActivity, users)
                        recyclerView.adapter = adapter
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this@FollowersActivity, "Error al cargar los datos de seguidores/seguidos", Toast.LENGTH_SHORT).show()
        }
    }
}