package com.example.tripshare.Map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class FragmentMap : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val markers = mutableListOf<Marker>()
    private var useFlags = false
    private var currentSizeFactor = 1.0f
    private var groupPinsByCountry = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Obtener referencia al Bottom Sheet
        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottom_sheet)

        val useFlagsSwitch = view.findViewById<Switch>(R.id.use_flags_switch)
        useFlagsSwitch.setOnCheckedChangeListener { _, isChecked ->
            useFlags = isChecked
            updateMarkerIcons() // Actualizar los iconos de los marcadores cuando el switch cambie
        }
        val groupPinsSwitch = view.findViewById<Switch>(R.id.group_pins_switch)
        groupPinsSwitch.setOnCheckedChangeListener { _, isChecked ->
            groupPinsByCountry = isChecked
            getLocationsFromFirebase()
        }

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
        discreteSlider.addOnChangeListener { _, value, _ ->
            // Usar la lógica para determinar el tamaño del marcador en función del valor
            currentSizeFactor = when (value) {
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
            setMarkerSize(currentSizeFactor) // Ajustar el tamaño de los marcadores según el valor
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
    private fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get().addOnSuccessListener { result ->
            mapViewModel.selectedLocations.clear()
            map.clear()
            markers.clear()

            val countryMarkers = mutableMapOf<String, LatLng>()

            for (document in result) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val address = document.getString("address")

                val latLng = LatLng(latitude, longitude)
                val country = getCountryByLatLng(latLng)

                if (groupPinsByCountry) {
                    if (country != null && !countryMarkers.containsKey(country)) {
                        val capitalLatLng = getCapitalLatLng(country) ?: latLng
                        countryMarkers[country] = capitalLatLng
                    }
                } else {
                    countryMarkers[latLng.toString()] = latLng
                }
            }

            for ((_, latLng) in countryMarkers) {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                )
                marker?.let { markers.add(it) }
            }
        }.addOnFailureListener { exception ->
            Log.e("FragmentMap", "Error al obtener las ubicaciones", exception)
        }
    }

    private fun getCapitalLatLng(countryCode: String): LatLng? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocationName(getCountryNameByCode(countryCode), 1)
            addresses?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            Log.e("FragmentMap", "Error obteniendo la capital", e)
            null
        }
    }

    private fun getCountryNameByCode(countryCode: String): String {
        val locale = Locale("", countryCode)
        return locale.displayCountry
    }


    // Función para cambiar el tamaño de todos los marcadores
    private fun setMarkerSize(sizeFactor: Float) {
        for (marker in markers) {
            val location = marker.position
            val country = getCountryByLatLng(location)

            if (useFlags && country != null) {
                // Si el switch està activat, canviar la mida de la bandera
                loadFlagForCountry(country) { flagUrl ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val bitmap = withContext(Dispatchers.IO) {
                            loadImageFromUrl(flagUrl)
                        }
                        val resizedBitmap = resizeBitmap(bitmap, sizeFactor) // Redimensionar la bandera
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
                        marker.setIcon(bitmapDescriptor)
                    }
                }
            } else {
                // Si el switch està desactivat, canviar la mida del marcador per defecte
                val markerIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_marker)
                val resizedIcon = resizeBitmap(markerIcon, sizeFactor) // Redimensionar el marcador normal
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon))
            }
        }
    }
    private fun resizeBitmap(bitmap: Bitmap, sizeFactor: Float): Bitmap {
        val width = (bitmap.width * sizeFactor).toInt()
        val height = (bitmap.height * sizeFactor).toInt()

        return if (width > 0 && height > 0) {
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        } else {
            bitmap // Si la mida és incorrecta, retorna el bitmap original
        }
    }

    private fun updateMarkerIcons() {
        for (marker in markers) {
            val location = marker.position
            val country = getCountryByLatLng(location)

            if (useFlags && country != null) {
                loadFlagForCountry(country) { flagUrl ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val bitmap = withContext(Dispatchers.IO) {
                            loadImageFromUrl(flagUrl)
                        }
                        val resizedBitmap = resizeBitmap(bitmap, currentSizeFactor) // Manté la mida correcta
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
                        marker.setIcon(bitmapDescriptor)
                    }
                }
            } else {
                val markerIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_marker)
                val resizedIcon = resizeBitmap(markerIcon, currentSizeFactor) // Manté la mida correcta
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon))
            }
        }
    }

    private fun loadFlagForCountry(countryCode: String, callback: (String) -> Unit) {
        val flagUrl = "https://flagcdn.com/w40/${countryCode.lowercase()}.png"
        callback(flagUrl)
    }
    private fun loadImageFromUrl(url: String): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val inputStream = connection.inputStream
        return BitmapFactory.decodeStream(inputStream)
    }
    private fun getCountryByLatLng(latLng: LatLng): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull()?.countryCode // Retorna el codi de país (ex: "US", "ES")
        } catch (e: Exception) {
            Log.e("FragmentMap", "Error obtenint el país", e)
            null
        }
    }
}
