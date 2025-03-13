package com.example.tripshare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.tripshare.Account.FragmentAccount
import com.example.tripshare.Locations.FragmentLocations
import com.example.tripshare.Map.FragmentMap
import com.example.tripshare.Pins.FragmentPins
import com.example.tripshare.Search.FragmentSearch
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        navigation = findViewById(R.id.navMenu)

        navigation.setOnItemSelectedListener {
            when (it.itemId){
                R.id.itemFragment1->{
                    val bundle = bundleOf("Param1" to "paramatre 1", "Param2" to "parametre 2")
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<FragmentAccount>(R.id.fragmentContainer, args = bundle)
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.itemFragment2->{
                    val bundle = bundleOf("Param1" to "paramatre 3", "Param2" to "parametre 4")
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<FragmentLocations>(R.id.fragmentContainer, args = bundle)
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.itemFragment3->{
                    val bundle = bundleOf("Param1" to "paramatre 1", "Param5" to "parametre 6")
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<FragmentPins>(R.id.fragmentContainer, args = bundle)
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.itemFragment4->{
                    val bundle = bundleOf("Param1" to "paramatre 1", "Param5" to "parametre 6")
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<FragmentMap>(R.id.fragmentContainer, args = bundle)
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.itemFragment5->{
                    val bundle = bundleOf("Param1" to "paramatre 1", "Param5" to "parametre 6")
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<FragmentSearch>(R.id.fragmentContainer, args = bundle)
                    }
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        val bundle = bundleOf("Param1" to "paramater 1", "Param2" to "paramater 2")
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<FragmentSearch>(R.id.fragmentContainer, args = bundle)
        }
    }
}