package com.example.tripshare.Locations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Location
import com.example.tripshare.R

class LocationAdapter(private val countryList: List<CountryWithLocations>) :
    RecyclerView.Adapter<LocationAdapter.CountryViewHolder>() {

    class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryTextView: TextView = view.findViewById(R.id.countryTextView)
        val locationsRecyclerView: RecyclerView = view.findViewById(R.id.locationsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val countryWithLocations = countryList[position]
        holder.countryTextView.text = countryWithLocations.country

        // Configurar RecyclerView per a les adreces dins del país
        holder.locationsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.locationsRecyclerView.adapter = AddressAdapter(countryWithLocations.locations)
        holder.locationsRecyclerView.visibility = View.VISIBLE
    }

    override fun getItemCount() = countryList.size
}

