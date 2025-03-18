package com.example.tripshare.Account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class FragmentAccount : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnAddPhoto: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSettings: ImageButton
    private lateinit var pubNumTextView: TextView  // Contador de fotos

    private val PICK_IMAGE = 1
    private val photosList = mutableListOf<Photo>()
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
        pubNumTextView = view.findViewById(R.id.pubNum)

        recyclerView.layoutManager = GridLayoutManager(context, 3)

        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        adapter = PhotosAdapter(userUid)

        adapter.onImageClickListener = { imageUri ->
            val intent = Intent(activity, ViewImageActivity::class.java)
            intent.putExtra("imageUri", imageUri)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        btnEditProfile.setOnClickListener { openEditProfile() }
        btnAddPhoto.setOnClickListener { openGallery() }
        btnSettings.setOnClickListener { openSettings() }

        loadUserProfile()
        loadUserPhotos()
        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*" // Solo imágenes
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                val mimeType = context?.contentResolver?.getType(selectedImageUri)
                if (mimeType != null && mimeType.startsWith("image")) {
                    showAddDescriptionDialog(selectedImageUri)
                } else {
                    Toast.makeText(context, "Solo se permiten imágenes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddDescriptionDialog(imageUri: Uri) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_description, null)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.edit_description)
        val editTextLocation = dialogView.findViewById<AutoCompleteTextView>(R.id.edit_location)
        val imageViewPreview = dialogView.findViewById<ImageView>(R.id.image_preview)
        imageViewPreview.setImageURI(imageUri)

        // Cargar localidades desde Firestore
        loadLocations(editTextLocation)

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva publicación")
            .setView(dialogView)
            .setPositiveButton("Publicar") { _, _ ->
                val description = editTextDescription.text.toString()
                val location = editTextLocation.text.toString()
                if (location.isNotEmpty()) {
                    addPhotoToGallery(imageUri, description, location)
                } else {
                    Toast.makeText(context, "Debes seleccionar una ubicación válida", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addPhotoToGallery(imageUri: Uri, description: String, location: String) {
        val userUid = user?.uid ?: return
        val photosRef = db.collection("users").document(userUid).collection("photos")

        val photoData = hashMapOf(
            "imageUrl" to imageUri.toString(),
            "description" to description,
            "location" to location,
            "timestamp" to System.currentTimeMillis()
        )

        // Agregar foto a la galería
        photosRef.add(photoData)
            .addOnSuccessListener {
                Toast.makeText(context, "Foto publicada correctamente", Toast.LENGTH_SHORT).show()
                loadUserPhotos() // Recargar fotos y actualizar el contador correctamente
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        user?.let {
            val userUid = it.uid
            val userDocRef = db.collection("users").document(userUid)

            userDocRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    userDocRef.set(mapOf("photoCount" to 0))
                }

                profileName.text = document.getString("name")
                document.getString("imageUri")?.let { uri ->
                    profileImage.setImageURI(Uri.parse(uri))
                }
            }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserPhotos() {
        val userUid = user?.uid ?: return
        val photosRef = db.collection("users").document(userUid).collection("photos")

        photosRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                photosList.clear() // Limpia la lista antes de agregar nuevas fotos

                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: continue
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""

                    photosList.add(Photo(imageUrl, description, location))
                }

                adapter.notifyDataSetChanged()
                updatePhotoCount(photosList.size)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePhotoCount(count: Int) {
        pubNumTextView.text = count.toString()
    }

    private fun loadLocations(editTextLocation: AutoCompleteTextView) {
        val userUid = user?.uid ?: return
        val locationsRef = db.collection("users").document(userUid).collection("locations")

        locationsRef.get()
            .addOnSuccessListener { documents ->
                val locationList = mutableListOf<String>()
                for (document in documents) {
                    document.getString("address")?.let { locationList.add(it) }
                }

                if (locationList.isNotEmpty()) {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locationList)
                    editTextLocation.setAdapter(adapter)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar ubicaciones", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openSettings() {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openEditProfile() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivity(intent)
    }
}