package com.example.vivs

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("send-location")
    fun sendLocation(@Body location: LocationData): Call<Void>
}

data class LocationData(
    val latitude: Double,
    val longitude: Double
)
