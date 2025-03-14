package com.example.tripshare.Locations

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentLocations : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private val groupedLocations = mutableListOf<CountryWithLocations>()
    private var isEditing = false
    private val originalLocations = mutableListOf<CountryWithLocations>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val editButton: Button = view.findViewById(R.id.btnEdit)
        editButton.setOnClickListener {
            isEditing = !isEditing
            locationAdapter.setEditingMode(isEditing)

            editButton.text = if (isEditing) "Cancelar" else "Editar"
        }
        val searchBar: EditText = view.findViewById(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterLocations(s.toString()) // Llamamos a la función de filtrado
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        locationAdapter = LocationAdapter(groupedLocations) { country, address ->
            deleteLocationFromFirebase(country, address)
        }
        recyclerView.adapter = locationAdapter

        getLocationsFromFirebase()

        return view
    }

    private fun deleteLocationFromFirebase(country: String, address: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.whereEqualTo("country", country)
            .whereEqualTo("address", address)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
                getLocationsFromFirebase()
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentLocations", "Error al eliminar ubicación", exception)
            }
    }

    private fun getLocationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("locations")

        locationsRef.get()
            .addOnSuccessListener { result ->
                val locationsMap = mutableMapOf<String, MutableList<String>>()

                for (document in result) {
                    val country = document.getString("country") ?: "País desconocido"
                    val address = document.getString("address") ?: "Dirección desconocida"

                    if (!locationsMap.containsKey(country)) {
                        locationsMap[country] = mutableListOf()
                    }
                    locationsMap[country]?.add(address)
                }

                originalLocations.clear()
                groupedLocations.clear()

                val sortedCountries = locationsMap.keys.sorted()

                for (country in sortedCountries) {
                    val addresses = locationsMap[country] ?: listOf()
                    val countryWithLocations = CountryWithLocations(country, addresses)
                    originalLocations.add(countryWithLocations)
                    groupedLocations.add(countryWithLocations)
                }

                locationAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentLocations", "Error al cargar ubicaciones", exception)
            }
    }
    private fun filterLocations(query: String) {
        groupedLocations.clear()

        if (query.isEmpty()) {
            groupedLocations.addAll(originalLocations) // Restaurar lista original
        } else {
            for (countryWithLocations in originalLocations) {
                val filteredAddresses = countryWithLocations.locations.filter {
                    it.contains(query, ignoreCase = true)
                }
                if (filteredAddresses.isNotEmpty()) {
                    groupedLocations.add(CountryWithLocations(countryWithLocations.country, filteredAddresses))
                }
            }
        }

        locationAdapter.notifyDataSetChanged()
    }


}
