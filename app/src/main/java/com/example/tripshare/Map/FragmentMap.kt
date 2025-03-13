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

    // Se infla la vista del fragmento y se establece el menú emergente para opciones del mapa
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Botón para mostrar el menú de opciones del mapa
        val mapOptionButton: ImageButton = view.findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(requireContext(), mapOptionButton)
        popupMenu.menuInflater.inflate(R.menu.map_options, popupMenu.menu)

        // Configuración de lo que ocurre cuando se selecciona una opción en el menú emergente
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }
        // Mostrar el menú cuando se hace clic en el botón
        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }

        return view
    }

    // Cambiar el tipo de mapa dependiendo de la opción seleccionada
    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    // Se ejecuta después de que la vista del fragmento ha sido creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Se obtiene el fragmento del mapa y se prepara para ser usado
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Método que se ejecuta cuando el mapa está listo para ser utilizado
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationsFromFirebase() // Obtener ubicaciones desde Firebase
        displaySelectedLocations() // Mostrar las ubicaciones seleccionadas en el mapa
    }

    // Muestra las ubicaciones seleccionadas en el mapa
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
    // Método que obtiene las ubicaciones desde Firebase
    fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get().addOnSuccessListener { result ->
            mapViewModel.selectedLocations.clear() // Limpiar la lista antes de cargar nuevas ubicaciones
            map.clear() // Limpiar el mapa

            // Se recorren los documentos obtenidos de Firebase y se agregan las ubicaciones al mapa
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
            // En caso de que ocurra un error al obtener las ubicaciones, se imprime un mensaje de error
            .addOnFailureListener { exception ->
                Log.e("FragmentMap", "Error al obtener las ubicaciones", exception)
            }
    }
}
