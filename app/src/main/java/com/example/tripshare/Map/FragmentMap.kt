package com.example.tripshare.Map

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tripshare.MapViewModel
import com.example.tripshare.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentMap : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapOptionButton: ImageButton = view.findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(requireContext(), mapOptionButton)
        popupMenu.menuInflater.inflate(R.menu.map_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }
        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }

        return view
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationsFromFirebase()
        displaySelectedLocations()
    }

    private fun displaySelectedLocations() {
        for (location in mapViewModel.selectedLocations) {
            val latLng = LatLng(location.latitude, location.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
            )

        }
    }
    fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val address = document.getString("address")

                    val latLng = LatLng(latitude, longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(address)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentMap", "Error al obtener las ubicaciones", exception)
            }
    }
}
