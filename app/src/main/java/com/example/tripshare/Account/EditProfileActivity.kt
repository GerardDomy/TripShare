package com.example.tripshare.Account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity: AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var editName: EditText
    private lateinit var btnSave: Button
    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.edit_profile_image)
        editName = findViewById(R.id.edit_profile_name)
        btnSave = findViewById(R.id.btn_save_profile)

        profileImage.setOnClickListener { openGallery() }
        btnSave.setOnClickListener { saveProfileChanges() }

        loadUserProfile()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            if (imageUri != null) {
                contentResolver.takePersistableUriPermission(
                    imageUri!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                profileImage.setImageURI(imageUri)
            }
        }
    }

    private fun saveProfileChanges() {
        val name = editName.text.toString()

        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentImageUri = document.getString("imageUri")
                        val imageUrl = imageUri?.toString() ?: currentImageUri ?: ""

                        val userData = hashMapOf(
                            "uid" to it.uid,
                            "email" to it.email,
                            "name" to name,
                            "imageUri" to imageUrl
                        )

                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra("name", name)
                                    putExtra("imageUri", imageUrl)
                                })
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    private fun loadUserProfile() {
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        editName.setText(document.getString("name"))
                        document.getString("imageUri")?.let { uri ->
                            profileImage.setImageURI(Uri.parse(uri))
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }
    /*

    private fun saveProfileChanges() {
        val intent = Intent()
        intent.putExtra("name", editName.text.toString())
        intent.putExtra("imageUri", imageUri.toString())
        setResult(RESULT_OK, intent)
        finish()
    }*/
}