package com.example.tripshare.Map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tripshare.Locations.Location
import com.example.tripshare.MapViewModel
import com.example.tripshare.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentMap : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val markers = mutableListOf<Marker>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Obtener referencia al Bottom Sheet
        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottom_sheet)

        // Verificar que bottomSheet no sea nulo
        if (bottomSheet == null) {
            Log.e("FragmentMap", "Bottom Sheet view not found!")
            return view // o lanza una excepción si es crítico
        }

        // Inicializar el BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Configurar el comportamiento del Bottom Sheet
        bottomSheetBehavior.peekHeight = 300 // Altura inicial (mitad de la pantalla)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // Estado inicial: oculto

        // Configurar el listener para manejar los estados del Bottom Sheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                /*when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Ocultar solo cuando el BottomSheet esté completamente abierto
                        view.findViewById<FloatingActionButton>(R.id.mapOptionButton).visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_HIDDEN,
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Mostrar cuando el BottomSheet esté a la mitad o cerrado
                        view.findViewById<FloatingActionButton>(R.id.mapOptionButton).visibility = View.VISIBLE
                    }
                }*/
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Manejar el deslizamiento si es necesario
            }
        })

        // Configurar eventos de clic para los estilos de mapa
        view.findViewById<Button>(R.id.use_style_1_button).setOnClickListener {
            // Cambiar a mapa normal
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.setMapStyle(null)
        }

        view.findViewById<Button>(R.id.use_style_2_button).setOnClickListener {
            // Cambiar a mapa híbrido
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            map.setMapStyle(null)
        }

        view.findViewById<Button>(R.id.use_style_3_button).setOnClickListener {
            // Cambiar a mapa satellite
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            map.setMapStyle(null)
        }

        view.findViewById<Button>(R.id.use_style_4_button).setOnClickListener {
            // Cambiar a mapa terrain
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            map.setMapStyle(null)
        }

        view.findViewById<Button>(R.id.use_style_5_button).setOnClickListener {
            changeMapStyle(R.raw.map_black)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        view.findViewById<Button>(R.id.use_style_6_button).setOnClickListener {
            changeMapStyle(R.raw.map_blacksatelit)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        view.findViewById<Button>(R.id.use_style_7_button).setOnClickListener {
            changeMapStyle(R.raw.map_silver)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        view.findViewById<Button>(R.id.use_style_8_button).setOnClickListener {
            changeMapStyle(R.raw.map_blue)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        view.findViewById<Button>(R.id.use_style_9_button).setOnClickListener {
            changeMapStyle(R.raw.map_blue_light)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        view.findViewById<Button>(R.id.use_style_10_button).setOnClickListener {
            changeMapStyle(R.raw.map_vintage)
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        // Inicializar el BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Permitir expansión total
        bottomSheetBehavior.isFitToContents = false // Permite que se expanda más allá de su contenido
        bottomSheetBehavior.halfExpandedRatio = 0.5f // Se abrirá hasta la mitad primero
        bottomSheetBehavior.peekHeight = 0 // Ocultar completamente al inicio
        bottomSheetBehavior.isHideable = true // Permite ocultarlo completamente
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // Estado inicial oculto

        // Configurar el Slider para cambiar el tamaño del marcador
        val discreteSlider = view.findViewById<Slider>(R.id.discreteSlider)

// Establecer el tamaño inicial del marcador
        val initialMarkerSize = 40f / 100f  // Equivalente a 40 en el rango de 0-100
        setMarkerSize(initialMarkerSize) // Usar el valor inicial para establecer el tamaño

// Configurar el listener para cambiar el tamaño del marcador
        discreteSlider.addOnChangeListener { slider, value, fromUser ->
            // Usar la lógica para determinar el tamaño del marcador en función del valor
            val sizeFactor = when (value) {
                in 0f..10f -> 0.6f  // 40% més petit que 40 (40 * 0.6)
                in 10f..20f -> 0.7f  // 30% més petit que 40 (40 * 0.7)
                in 20f..30f -> 0.8f  // 20% més petit que 40 (40 * 0.8)
                in 30f..40f -> 0.9f  // 10% més petit que 40 (40 * 0.9)
                in 40f..50f -> 1.0f  // 40 (100%) normal
                in 50f..60f -> 1.1f  // 10% més gran que 40 (40 * 1.1)
                in 60f..70f -> 1.2f  // 20% més gran que 40 (40 * 1.2)
                in 70f..80f -> 1.3f  // 30% més gran que 40 (40 * 1.3)
                in 80f..90f -> 1.4f  // 40% més gran que 40 (40 * 1.4)
                in 90f..100f -> 1.5f // 50% més gran que 40 (40 * 1.5)
                else -> 1.6f        // 60% més gran que 40 (40 * 1.6)
            }
            setMarkerSize(sizeFactor) // Ajustar el tamaño de los marcadores según el valor
        }

        // Configurar el OnClickListener para el botón mapOptionsMenu
        val mapOptionsMenu = view.findViewById<ImageButton>(R.id.mapOptionButton)
        mapOptionsMenu.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                else -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        return view
    }

    private fun changeMapStyle(styleResId: Int) {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), styleResId))
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
            val marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
            )
            marker?.let { markers.add(it) }
        }
    }

    fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get().addOnSuccessListener { result ->
            mapViewModel.selectedLocations.clear()
            map.clear()

            for (document in result) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val address = document.getString("address")

                val latLng = LatLng(latitude, longitude)
                mapViewModel.selectedLocations.add(Location(latitude, longitude, address, null))

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(address)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                )
                marker?.let { markers.add(it) }
            }
        }.addOnFailureListener { exception ->
            Log.e("FragmentMap", "Error al obtener las ubicaciones", exception)
        }
    }
    // Función para cambiar el tamaño de todos los marcadores
    private fun setMarkerSize(sizeFactor: Float) {
        for (marker in markers) {
            val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)
            val resizedIcon = resizeMarkerIcon(markerIcon, sizeFactor)

            // Establecer el nuevo icono al marcador
            marker.setIcon(resizedIcon)
        }
    }

    // Método para redimensionar el icono del marcador
    private fun resizeMarkerIcon(icon: BitmapDescriptor, sizeFactor: Float): BitmapDescriptor {
        // Obtener el bitmap original del descriptor
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_marker)

        // Calcular el nuevo tamaño en base al factor
        val width = (bitmap.width * sizeFactor).toInt()
        val height = (bitmap.height * sizeFactor).toInt()

        // Asegurarse de que el ancho y la altura sean mayores que 0
        if (width <= 0 || height <= 0) {
            Log.e("FragmentMap", "Invalid width or height for resized marker: $width x $height")
            return icon // Retornar el icono original si el tamaño es inválido
        }

        // Redimensionar el bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // Convertir el bitmap redimensionado a un BitmapDescriptor
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }
}
