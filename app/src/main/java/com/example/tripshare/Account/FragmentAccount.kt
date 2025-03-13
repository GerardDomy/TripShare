package com.example.tripshare.Account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentAccount : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnAddPhoto: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSettings: ImageButton


    private val PICK_IMAGE = 1
    private val photosList = mutableListOf<Uri>()
    private lateinit var adapter: PhotosAdapter

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        profileName = view.findViewById(R.id.username)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        btnAddPhoto = view.findViewById(R.id.btn_add_photo)
        recyclerView = view.findViewById(R.id.recyclerViewPhotos)
        btnSettings = view.findViewById(R.id.btnSettings)

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        adapter = PhotosAdapter(photosList)

        adapter.onImageClickListener = { imageUri ->
            val intent = Intent(activity, ViewImageActivity::class.java)
            intent.putExtra("imageUri", imageUri.toString())
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        btnEditProfile.setOnClickListener { openEditProfile() }
        btnAddPhoto.setOnClickListener { openGallery() }
        btnSettings.setOnClickListener { openSettings()}

        loadUserProfile()
        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data != null)
        {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let {
                photosList.add(0, it)
                adapter.notifyDataSetChanged()
                incrementPublicationCount()
            }
        }
        else if (requestCode == 1000 && resultCode == AppCompatActivity.RESULT_OK && data != null)
        {
            val newName = data.getStringExtra("name")
            val newImageUri = data.getStringExtra("imageUri")

            if (newImageUri != null) {
                profileImage.setImageURI(Uri.parse(newImageUri))
            }
            profileName.text = newName
        }
    }

    private fun incrementPublicationCount() {
        val pubNumTextView = view?.findViewById<TextView>(R.id.pubNum)
        pubNumTextView?.let {
            val currentCount = it.text.toString().toIntOrNull() ?: 0
            it.text = (currentCount + 1).toString()
        }
    }
    private fun openSettings(){
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openEditProfile() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivityForResult(intent, PICK_IMAGE)
    }
    private fun loadUserProfile() {
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        profileName.text = document.getString("name")
                        document.getString("imageUri")?.let { uri ->
                            profileImage.setImageURI(Uri.parse(uri))
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
