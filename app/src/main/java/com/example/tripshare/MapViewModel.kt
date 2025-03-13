package com.example.tripshare

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MapViewModel : ViewModel() {

    val selectedLocations = mutableListOf<Location>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveLocationsToFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("MapViewModel", "Usuario no autenticado")
            return
        }

        val locationsRef = db.collection("users").document(userId).collection("locations")

        for (location in selectedLocations) {
            val locationMap = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "address" to location.address,
                "country" to location.country
            )

            locationsRef.add(locationMap)
                .addOnSuccessListener { documentReference ->
                    Log.d("MapViewModel", "Ubicación guardada con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("MapViewModel", "Error al guardar ubicación", e)
                }
        }
    }
}




