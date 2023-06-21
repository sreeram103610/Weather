package com.maadlabs.weather.search.data.api

import com.maadlabs.weather.search.data.dto.WeatherSourceData
import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

internal interface WeatherApi {
    @GET(".")
    suspend fun currentWeather(@Query("q") cityName: String): DataResult<WeatherSourceData, ErrorResponse>

    @GET(".")
    suspend fun currentWeather(@Query("lat") latitude: String, @Query("lon") longitude: String): DataResult<WeatherSourceData, ErrorResponse>

    @GET(".")
    @Headers("Cache-Control: no-cache")
    suspend fun currentWeatherNoCache(@Query("q") cityName: String): DataResult<WeatherSourceData, ErrorResponse>

    @GET(".")
    @Headers("Cache-Control: no-cache")
    suspend fun currentWeatherNoCache(@Query("lat") latitude: String, @Query("lon") longitude: String): DataResult<WeatherSourceData, ErrorResponse>
}
