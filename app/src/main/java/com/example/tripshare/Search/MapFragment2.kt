package com.example.tripshare.Search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tripshare.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment2 : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var viewedUserUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map2, container, false)

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)

        // Retrieve viewedUserUid from arguments
        viewedUserUid = arguments?.getString("viewedUserUid")

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Use the passed viewedUserUid to load locations
        loadLocationsFromFirebase(viewedUserUid)
    }

    private fun loadLocationsFromFirebase(viewedUserUid: String?) {
        // Use the passed viewedUserUid to load locations
        if (viewedUserUid == null) {
            Log.e("MapFragment2", "viewedUserUid is null")
            return
        }

        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(viewedUserUid)
            .collection("locations")

        locationsRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val latLng = LatLng(latitude, longitude)

                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(document.getString("address") ?: "Location") // Use address as title
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                )
            }

        }.addOnFailureListener { exception ->
            Log.e("MapFragment2", "Error getting locations: ", exception)
        }
    }
}


