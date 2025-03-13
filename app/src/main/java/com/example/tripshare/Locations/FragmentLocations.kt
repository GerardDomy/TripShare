package com.example.tripshare.Locations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Location
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentLocations : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private val groupedLocations = mutableListOf<CountryWithLocations>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        locationAdapter = LocationAdapter(groupedLocations)
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
                val locationsMap = mutableMapOf<String, MutableList<String>>()

                for (document in result) {
                    val country = document.getString("country") ?: "País desconegut"
                    val address = document.getString("address") ?: "Adreça desconeguda"

                    if (!locationsMap.containsKey(country)) {
                        locationsMap[country] = mutableListOf()
                    }
                    locationsMap[country]?.add(address)
                }

                groupedLocations.clear()
                for ((country, addresses) in locationsMap) {
                    groupedLocations.add(CountryWithLocations(country, addresses))
                }

                locationAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentLocations", "Error al carregar ubicacions", exception)
            }
    }
}
