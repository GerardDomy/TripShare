package com.example.tripshare.Locations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripshare.R
import java.util.Locale

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
        val flagImageView: ImageView = itemView.findViewById(R.id.flagImageView)  // Asegúrate de que el ID coincide
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

        val flagUrl = getFlagUrl(countryWithLocations.country)
        Glide.with(holder.itemView.context)
            .load(flagUrl)
            .into(holder.flagImageView)


        holder.locationsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.locationsRecyclerView.adapter = AddressAdapter(
            countryWithLocations.country,
            countryWithLocations.locations,
            isEditing,
            onDeleteClick
        )

        holder.locationsRecyclerView.visibility = if (countryWithLocations.isExpanded) View.VISIBLE else View.GONE

        holder.arrowIcon.rotation = if (countryWithLocations.isExpanded) 0f else -90f

        holder.countryContainer.setOnClickListener {
            countryWithLocations.isExpanded = !countryWithLocations.isExpanded
            notifyItemChanged(position) // Refrescar el item
        }
    }
    private fun getFlagUrl(countryName: String?): String? {
        if (countryName.isNullOrEmpty()) return null
        val countryCode = Locale.getISOCountries().find {
            Locale("", it).displayCountry.equals(countryName, ignoreCase = true)
        }
        return countryCode?.let { "https://flagcdn.com/w320/${it.lowercase()}.png" }
    }


    override fun getItemCount() = countryList.size
}
