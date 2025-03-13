package com.example.tripshare


data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val country: String? = ""
)
