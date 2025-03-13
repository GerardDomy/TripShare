package com.example.tripshare.Locations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.R

class AddressAdapter(private val addresses: List<String>) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.addressTextView.text = addresses[position]
    }

    override fun getItemCount() = addresses.size
}