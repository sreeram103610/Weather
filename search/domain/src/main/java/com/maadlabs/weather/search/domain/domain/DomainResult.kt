package com.maadlabs.weather.search.domain.domain

sealed interface WeatherDomainResult {
    object Loading: WeatherDomainResult
    data class Error(val errorType: DomainError): WeatherDomainResult
    data class WeatherData(
        val city: String,
        val weatherData: WeatherDomainData
    ): WeatherDomainResult
    data class WeatherDataForLocation(
        val weatherData: WeatherDomainData
    ): WeatherDomainResult
    data class RefreshedWeatherData(
        val city: String,
        val weatherData: WeatherDomainData
    ): WeatherDomainResult
    object Default : WeatherDomainResult
}

enum class DomainError {
    NETWORK_ERROR, SERVER_ERROR, UNKNOWN_ERROR
}