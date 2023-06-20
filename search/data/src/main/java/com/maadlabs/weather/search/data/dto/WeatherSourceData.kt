package com.maadlabs.weather.search.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MainSourceData(@Json(name = "temp") val temperature: Float,
                    @Json(name = "temp_min") val minTemperature: Float,
                    @Json(name = "temp_max") val maxTemperature: Float)

@JsonClass(generateAdapter = true)
internal data class WeatherSourceData(
    @Json(name = "name") val locationName: String,
    @Json(name = "main") val mainData: MainSourceData, )
