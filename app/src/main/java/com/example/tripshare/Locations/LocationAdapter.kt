package com.example.tripshare.Locations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R

class LocationAdapter(

    private val countryList: List<CountryWithLocations>,
    private val onDeleteClick: (String, String) -> Unit

) : RecyclerView.Adapter<LocationAdapter.CountryViewHolder>() {

    var isEditing: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryTextView: TextView = view.findViewById(R.id.countryTextView)
        val locationsRecyclerView: RecyclerView = view.findViewById(R.id.locationsRecyclerView)
        val arrowIcon: ImageView = view.findViewById(R.id.arrowIcon)  // Asegúrate de que esté en item_country.xml
        val countryContainer: LinearLayout = view.findViewById(R.id.countryContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }
    fun setEditingMode(editing: Boolean) {
        isEditing = editing
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val countryWithLocations = countryList[position]
        holder.countryTextView.text = countryWithLocations.country

        holder.locationsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.locationsRecyclerView.adapter = AddressAdapter(
            countryWithLocations.country,
            countryWithLocations.locations,
            isEditing,
            onDeleteClick
        )
        // Mostrar u ocultar la lista según isExpanded
        holder.locationsRecyclerView.visibility = if (countryWithLocations.isExpanded) View.VISIBLE else View.GONE
        // Rotar la flecha según isExpanded
        holder.arrowIcon.rotation = if (countryWithLocations.isExpanded) 0f else -90f

        // Manejar clic en el contenedor del país
        holder.countryContainer.setOnClickListener {
            countryWithLocations.isExpanded = !countryWithLocations.isExpanded
            notifyItemChanged(position) // Refrescar el item
        }
    }



    override fun getItemCount() = countryList.size
}


