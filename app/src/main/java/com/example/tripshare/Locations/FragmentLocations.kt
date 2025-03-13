package com.example.tripshare.Locations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Location
import com.example.tripshare.MapViewModel
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentLocations : Fragment() {

    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private val locationsList = mutableListOf<Location>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        locationAdapter = LocationAdapter(locationsList)
        recyclerView.adapter = locationAdapter

        getLocationsFromFirebase()

        return view
    }

    private fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get()
            .addOnSuccessListener { result ->
                locationsList.clear()
                for (document in result) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val address = document.getString("address") ?: "Dirección desconocida"
                    val country = document.getString("country") ?: "País desconocido"

                    val location = Location(latitude, longitude, address, country)
                    locationsList.add(location)
                }
                locationAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentLocations", "Error al obtener ubicaciones", exception)
            }
    }
}
