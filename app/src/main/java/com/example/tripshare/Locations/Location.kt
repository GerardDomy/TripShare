package com.example.tripshare.Locations


data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val country: String? = ""
)
