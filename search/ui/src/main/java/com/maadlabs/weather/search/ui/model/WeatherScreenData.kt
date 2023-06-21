package com.maadlabs.weather.search.ui.model

data class WeatherScreenData(
    val temperature: String,
    val minTemperature: String,
    val maxTemperature: String,
    val imageUri: String,
    val cityName: String
)