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
import android.widget.FrameLayout
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID


class FragmentAccount : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnAddPhoto: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSettings: ImageButton
    private lateinit var pubNumTextView: TextView  // Contador de fotos
    private lateinit var seguidoresNumTextView: TextView
    private lateinit var seguidosNumTextView: TextView
    private lateinit var btnSeguidores: FrameLayout
    private lateinit var btnSeguidos: FrameLayout

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
        seguidoresNumTextView = view.findViewById(R.id.seguidoresNum)
        seguidosNumTextView = view.findViewById(R.id.seguidosNum)
        btnSeguidores = view.findViewById(R.id.btn_seguidores)
        btnSeguidos = view.findViewById(R.id.btn_seguidos)

        btnSeguidores.setOnClickListener { openFollowersActivity(true) }
        btnSeguidos.setOnClickListener { openFollowersActivity(false) }

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

        val spanCount = if (screenWidthDp >= 600) 4 else 3  // 600dp o més = tablet

        recyclerView.layoutManager = GridLayoutManager(context, spanCount)



        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        adapter = PhotosAdapter(userUid)

        adapter.onImageClickListener = { imageUri ->
            val intent = Intent(activity, ViewImageActivity::class.java)
            intent.putExtra("imageUrl", imageUri)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        btnEditProfile.setOnClickListener { openEditProfile() }
        btnAddPhoto.setOnClickListener { openGallery() }
        btnSettings.setOnClickListener { openSettings() }

        loadFollowData()
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
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val photoRef = storageRef.child("user_photos/$userUid/$fileName")

        // Subir imagen a Storage
        photoRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                photoRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    savePhotoMetadataToFirestore(downloadUri.toString(), description, location)
                } else {
                    Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun savePhotoMetadataToFirestore(downloadUrl: String, description: String, location: String) {
        val userUid = user?.uid ?: return
        val photosRef = db.collection("users").document(userUid).collection("photos")

        val photoData = hashMapOf(
            "imageUrl" to downloadUrl,
            "description" to description,
            "location" to location,
            "timestamp" to System.currentTimeMillis()
        )

        photosRef.add(photoData)
            .addOnSuccessListener {
                Toast.makeText(context, "Foto publicada correctamente", Toast.LENGTH_SHORT).show()
                loadUserPhotos()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar la información de la foto", Toast.LENGTH_SHORT).show()
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
                document.getString("imageUrl")?.let { uri ->
                    if (uri.isNotEmpty()) {
                        Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.ic_fragment_account) // Ícono por defecto mientras carga
                            .error(R.drawable.ic_fragment_account) // Imagen si ocurre un error
                            .into(profileImage)
                    }
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

    private fun loadFollowData() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(userUid)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Obtener seguidores desde Firestore (se supone que se actualiza desde ActivityProfile)
                val seguidores = document.getLong("followersCount")?.toInt() ?: 0
                seguidoresNumTextView.text = seguidores.toString()

                // Obtener seguidos desde Firestore y calcular suma
                val seguidosRef = db.collection("users").document(userUid).collection("following")

                seguidosRef.get().addOnSuccessListener { followingDocs ->
                    val seguidosCount = followingDocs.size() // Contar los documentos en "following"
                    seguidosNumTextView.text = seguidosCount.toString()

                    // Guardar el número de seguidos en Firestore para persistencia
                    userDocRef.update("followingCount", seguidosCount)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error al cargar datos de seguidores/seguidos", Toast.LENGTH_SHORT).show()
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
    private fun openFollowersActivity(isFollowers: Boolean) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userUid).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Usuario"
                val intent = Intent(activity, FollowersActivity::class.java).apply {
                    putExtra("USER_NAME", userName)
                    putExtra("USER_ID", userUid)
                    putExtra("IS_FOLLOWERS", isFollowers)
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
            }
    }
}