package com.example.tripshare

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tripshare.Locations.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MapViewModel : ViewModel() {


    // Lista mutable para almacenar las ubicaciones seleccionadas
    val selectedLocations = mutableListOf<Location>()


    // Referencias a FirebaseFirestore y FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    // Método para guardar las ubicaciones seleccionadas en Firebase
    fun saveLocationsToFirebase() {
        // Obtiene el ID del usuario autenticado
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("MapViewModel", "Usuario no autenticado") // Si no hay usuario autenticado, se muestra un error
            return
        }


        // Referencia a la colección 'locations' del usuario en Firestore
        val locationsRef = db.collection("users").document(userId).collection("locations")


        // Recorre las ubicaciones seleccionadas y las guarda en Firestore
        for (location in selectedLocations) {
            // Se crea un mapa con los datos de la ubicación
            val locationMap = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "address" to location.address,
                "country" to location.country,
                "flagUrl" to location.flagUrl


            )


            // Se agrega la ubicación a la base de datos de Firestore
            locationsRef.add(locationMap)
                .addOnSuccessListener { documentReference ->
                    Log.d("MapViewModel", "Ubicación guardada con ID: ${documentReference.id}")
                }
                // Si ocurre un error al guardar, se muestra en el log
                .addOnFailureListener { e ->
                    Log.e("MapViewModel", "Error al guardar ubicación", e)
                }
        }
    }
}





