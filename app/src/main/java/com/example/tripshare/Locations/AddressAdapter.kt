package com.example.tripshare.Locations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R

class AddressAdapter(
    private val country: String,
    private val addresses: List<String>,
    private var isEditing: Boolean,
    private val onDeleteClick: (String, String) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {



    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val deleteButton: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.addressTextView.text = addresses[position]

        holder.deleteButton.visibility = if (isEditing) View.VISIBLE else View.GONE

        holder.deleteButton.setOnClickListener {
            onDeleteClick(country, addresses[position])
        }
    }



    override fun getItemCount() = addresses.size
}
