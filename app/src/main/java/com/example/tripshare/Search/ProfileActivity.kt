package com.example.tripshare.Search

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.tripshare.Account.FollowersActivity
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
    private lateinit var pubNumTextView: TextView
    private lateinit var seguidoresNumTextView: TextView
    private lateinit var seguidosNumTextView: TextView
    private lateinit var followButton: Button
    private lateinit var viewPager: ViewPager2
    private lateinit var leftButton: ImageButton
    private lateinit var rightButton: ImageButton


    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private var viewedUserUid: String? = null
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userNameText = findViewById(R.id.username)
        profileImageView = findViewById(R.id.profile_image)
        pubNumTextView = findViewById(R.id.pubNum)
        seguidoresNumTextView = findViewById(R.id.seguidoresNum)
        seguidosNumTextView = findViewById(R.id.seguidosNum)
        followButton = findViewById(R.id.btn_seguir)
        viewPager = findViewById(R.id.viewPagerProfile)
        leftButton = findViewById(R.id.btn_left)
        rightButton = findViewById(R.id.btn_right)
        viewPager.isUserInputEnabled = false


        leftButton.setOnClickListener {
            viewPager.currentItem = 1 // Cambia a la primera página (por ejemplo: fotos)
        }

        rightButton.setOnClickListener {
            viewPager.currentItem = 0 // Cambia a la segunda página (por ejemplo: mapa)
        }


        findViewById<FrameLayout>(R.id.btn_seguidores).setOnClickListener {
            openFollowersActivity(isFollowers = true)
        }

        findViewById<FrameLayout>(R.id.btn_seguidos).setOnClickListener {
            openFollowersActivity(isFollowers = false)
        }

        followButton.setOnClickListener {
            toggleFollow()
        }

        val userName = intent.getStringExtra("USER_NAME") ?: ""
        userNameText.text = userName

        loadUserInfo(userName)

    }

    private fun loadUserInfo(userName: String) {
        val formattedUserName = userName.trim().lowercase()

        db.collection("users")
            .whereEqualTo("name", formattedUserName)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    viewedUserUid = document.id
                    val userImageUri = document.getString("imageUri") ?: ""

                    if (userImageUri.isNotEmpty()) {
                        Picasso.get()
                            .load(userImageUri)
                            .placeholder(R.drawable.ic_fragment_account)
                            .into(profileImageView)
                    }

                    updateFollowButton()
                    loadFollowCounts()
                    loadUserPhotos(viewedUserUid!!)
                }
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

                    photosList.add(Photo(imageUrl, description, location))
                }

                pubNumTextView.text = photosList.size.toString()
                viewPager.adapter = ProfilePagerAdapter(this, viewedUserUid ?: "")
                viewPager.currentItem = 1
            }
    }
    private fun updateFollowButton() {
        viewedUserUid?.let { uid ->
            currentUserUid?.let { myUid ->
                db.collection("users").document(myUid).collection("following").document(uid)
                    .addSnapshotListener { document, _ ->
                        isFollowing = document?.exists() == true
                        followButton.text = if (isFollowing) "Siguiendo" else "Seguir"
                        if (isFollowing) {
                            followButton.setBackgroundColor(Color.TRANSPARENT)
                            followButton.setTextColor(Color.BLACK)
                            followButton.setTypeface(null, Typeface.BOLD)
                            followButton.setBackgroundResource(R.drawable.border_black)
                        } else {
                            followButton.setBackgroundResource(R.drawable.custom_button_background)
                            followButton.setTextColor(Color.WHITE)
                        }
                    }
            }
        }
    }

    private fun toggleFollow() {
        viewedUserUid?.let { uid ->
            currentUserUid?.let { myUid ->
                val userRef = db.collection("users").document(myUid).collection("following").document(uid)
                val viewedUserRef = db.collection("users").document(uid).collection("followers").document(myUid)

                if (isFollowing) {
                    userRef.delete()
                    viewedUserRef.delete()
                    updateFollowCount(uid, myUid, -1)
                } else {
                    userRef.set(mapOf("follow" to true))
                    viewedUserRef.set(mapOf("follow" to true))
                    updateFollowCount(uid, myUid, 1)
                }
            }
        }
    }
    private fun updateFollowCount(viewedUid: String, myUid: String, change: Int) {
        val viewedUserRef = db.collection("users").document(viewedUid)
        val myUserRef = db.collection("users").document(myUid)

        db.runTransaction { transaction ->
            val viewedUserSnapshot = transaction.get(viewedUserRef)
            val myUserSnapshot = transaction.get(myUserRef)

            val newSeguidores = (viewedUserSnapshot.getLong("followersCount") ?: 0) + change
            val newSeguidos = (myUserSnapshot.getLong("followingCount") ?: 0) + change

            transaction.update(viewedUserRef, "followersCount", newSeguidores.coerceAtLeast(0))
            transaction.update(myUserRef, "followingCount", newSeguidos.coerceAtLeast(0))
        }.addOnSuccessListener {
            loadFollowCounts()
        }
    }

    private fun loadFollowCounts() {
        viewedUserUid?.let { uid ->
            db.collection("users").document(uid)
                .addSnapshotListener { document, _ ->
                    if (document != null && document.exists()) {
                        seguidoresNumTextView.text = document.getLong("followersCount")?.toString() ?: "0"
                        seguidosNumTextView.text = document.getLong("followingCount")?.toString() ?: "0"
                    }
                }
        }
    }

    private fun openFollowersActivity(isFollowers: Boolean) {
        viewedUserUid?.let { uid ->
            val intent = Intent(this, FollowersActivity::class.java).apply {
                putExtra("USER_NAME", userNameText.text.toString())
                putExtra("USER_ID", uid)
                putExtra("IS_FOLLOWERS", isFollowers)
            }
            startActivity(intent)
        }
    }
}
