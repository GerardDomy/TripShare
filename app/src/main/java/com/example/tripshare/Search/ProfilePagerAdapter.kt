package com.example.tripshare.Search

import android.os.Bundle
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
            val fragment = MapFragment2()
            // Pass viewedUserUid as arguments
            fragment.arguments = Bundle().apply {
                putString("viewedUserUid", viewedUserUid)
            }
            return fragment
        } else {
            PhotosFragment(viewedUserUid)
        }
    }
}



