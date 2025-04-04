package com.example.tripshare.Search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Account.Photo
import com.example.tripshare.Account.PhotosAdapter
import com.example.tripshare.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PhotosFragment(private val userUid: String) : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photos, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPhotos)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = PhotosAdapter("")
        recyclerView.adapter = adapter

        db.collection("users").document(userUid).collection("photos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val photos = mutableListOf<Photo>()
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl") ?: continue
                    val description = document.getString("description") ?: ""
                    val location = document.getString("location") ?: ""
                    photos.add(Photo(imageUrl, description, location))
                }
                adapter.updatePhotos(photos)
            }

        return view
    }
}


