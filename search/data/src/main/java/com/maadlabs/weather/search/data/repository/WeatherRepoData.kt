package com.maadlabs.weather.search.data.repository

data class WeatherRepoData(
    val locationName: String,
    val temperature: String,
    val minTemperature: String,
    val maxTemperature: String,
    val imageUri: String
)
