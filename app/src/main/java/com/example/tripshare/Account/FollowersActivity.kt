package com.example.tripshare.Account

import android.content.Intent
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
import com.example.tripshare.Search.ProfileActivity
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
        adapter = UserAdapter(this, userList, { user ->
            // Aquí manejas el clic en un usuario
            navigateToProfile(user)
        },false)
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

    private fun navigateToProfile(user: User) {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("USER_NAME", user.name)
            putExtra("USER_ID", user.userId)
        }
        startActivity(intent)
    }

    private fun loadFollowersAndFollowing(isFollowers: Boolean) {
        val userUid = userId ?: return

        val collection = if (isFollowers) {
            db.collection("users").document(userUid).collection("followers")
        } else {
            db.collection("users").document(userUid).collection("following")
        }

        collection.get().addOnSuccessListener { documents ->
            val users = mutableListOf<User>()
            val total = documents.size()

            for (document in documents) {
                val userId = document.id

                db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                    val user = User(
                        userId = userDoc.id,
                        name = userDoc.getString("name") ?: "Unknown",
                        imageUri = userDoc.getString("imageUrl") ?: ""
                    )
                    users.add(user)

                    if (users.size == total) {
                        adapter = UserAdapter(this@FollowersActivity, users, { selectedUser ->
                            navigateToProfile(selectedUser)
                        },false)
                        recyclerView.adapter = adapter
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this@FollowersActivity, "Error al cargar los datos", Toast.LENGTH_SHORT)
                .show()
        }
    }
}