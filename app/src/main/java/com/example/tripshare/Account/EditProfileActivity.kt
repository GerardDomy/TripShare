package com.example.tripshare.Account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var editName: EditText
    private lateinit var btnSave: Button
    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.edit_profile_image)
        editName = findViewById(R.id.edit_profile_name)
        btnSave = findViewById(R.id.btn_save_profile)

        editName.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            source.toString().lowercase().replace(" ", "")
        })

        profileImage.setOnClickListener { openGallery() }
        btnSave.setOnClickListener { saveProfileChanges() }

        loadUserProfile()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                profileImage.setImageURI(it)
            }
        }
    }

    private fun saveProfileChanges() {
        val name = editName.text.toString()

        if (user == null) return

        val userDocRef = db.collection("users").document(user.uid)

        if (imageUri != null) {
            // Subir imagen a Firebase Storage
            val fileRef = storageRef.child("users/${user.uid}/profile.jpg")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        saveUserProfileToFirestore(name, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Guardar sin nueva imagen
            userDocRef.get().addOnSuccessListener { document ->
                val existingImageUrl = document.getString("imageUrl") ?: ""
                saveUserProfileToFirestore(name, existingImageUrl)
            }
        }
    }

    private fun saveUserProfileToFirestore(name: String, imageUrl: String) {
        val userDocRef = db.collection("users").document(user!!.uid)

        userDocRef.get().addOnSuccessListener { document ->
            val currentData = document.data?.toMutableMap() ?: mutableMapOf()
            currentData["name"] = name
            currentData["imageUrl"] = imageUrl

            userDocRef.set(currentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("name", name)
                        putExtra("imageUrl", imageUrl)
                    })
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserProfile() {
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        editName.setText(document.getString("name"))

                        val imageUrl = document.getString("imageUrl") ?: ""
                        if (imageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_fragment_account)
                                .into(profileImage)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
