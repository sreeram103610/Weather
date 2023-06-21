package com.maadlabs.weather.search.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationRepoData(val latitude: String, val longitude: String)
