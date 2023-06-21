package com.maadlabs.weather.search.data.repository

import com.maadlabs.weather.search.data.api.WeatherApi
import com.maadlabs.weather.search.data.dto.WeatherSourceData
import javax.inject.Inject


/**
 * Get the weather information.
 * Note that since we're using Okhttp caches, we don't use
 * a Offline Data Source
 */
interface WeatherRepository {

    suspend fun getCurrentWeather(useCachedData: Boolean, cityName: String): RepoResult<WeatherRepoData, RepoErrorType>
    suspend fun getCurrentWeather(useCachedData: Boolean, location: LocationRepoData): RepoResult<WeatherRepoData, RepoErrorType>
}

internal class DefaultWeatherRepository @Inject constructor(val weatherApi: WeatherApi) : WeatherRepository {

    override suspend fun getCurrentWeather(
        useCachedData: Boolean,
        cityName: String
    ): RepoResult<WeatherRepoData, RepoErrorType> {
        if (useCachedData) {
            return weatherApi.currentWeather(cityName.lowercase()).toRepoResult(::successMapper)
        }
        return weatherApi.currentWeatherNoCache(cityName.lowercase()).toRepoResult(::successMapper)
    }

    override suspend fun getCurrentWeather(
        useCachedData: Boolean,
        location: LocationRepoData
    ): RepoResult<WeatherRepoData, RepoErrorType> {
        if (useCachedData) {
            return weatherApi.currentWeather(latitude = location.latitude, longitude = location.longitude).toRepoResult(::successMapper)
        }
        return weatherApi.currentWeatherNoCache(latitude = location.latitude, longitude = location.longitude).toRepoResult(::successMapper)
    }

    private fun successMapper(data: WeatherSourceData): WeatherRepoData {
        return WeatherRepoData(
            data.locationName,
            data.mainData.temperature.toInt().toString(),
            data.mainData.minTemperature.toInt().toString(),
            data.mainData.maxTemperature.toInt().toString(),
            data.weather.first().icon.toImageUrl()
        )
    }

    private fun String.toImageUrl() = "https://openweathermap.org/img/wn/$this@2x.png"
}
