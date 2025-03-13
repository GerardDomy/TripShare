package com.example.tripshare.Pins

import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import com.example.tripshare.Location
import com.example.tripshare.MapViewModel
import com.example.tripshare.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Locale

class FragmentPins : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pins, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_search_key))
        }

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Toast.makeText(requireContext(), "Error en la búsqueda: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng!!
                val country = getCountryFromLatLng(latLng)

                val location = Location(latLng.latitude, latLng.longitude, place.address, country)
                mapViewModel.selectedLocations.add(location)

                Toast.makeText(requireContext(), "Ubicación seleccionada: ${place.address}",  Toast.LENGTH_SHORT).show()


                mapViewModel.saveLocationsToFirebase()

                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(place.name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
        })

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
    private fun getCountryFromLatLng(latLng: LatLng): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            return addresses[0].countryName
        }
        return null
    }
}