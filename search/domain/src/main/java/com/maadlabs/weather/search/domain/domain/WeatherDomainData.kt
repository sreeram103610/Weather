package com.maadlabs.weather.search.domain.domain

data class WeatherDomainData(
    val city: String,
    val temperature: String,
    val minTemperature: String,
    val maxTemperature: String,
    val imageUri: String
)

