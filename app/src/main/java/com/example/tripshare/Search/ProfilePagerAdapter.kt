package com.example.tripshare.Search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tripshare.Account.Photo
import com.google.android.gms.maps.MapFragment

class ProfilePagerAdapter(fragmentActivity: FragmentActivity, private val viewedUserUid: String) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            MapFragment2()
        } else {
            // Pasamos el viewedUserUid en lugar de la lista de fotos
            PhotosFragment(viewedUserUid)
        }
    }
}


