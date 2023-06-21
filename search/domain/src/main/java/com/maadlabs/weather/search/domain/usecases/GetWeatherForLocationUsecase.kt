package com.maadlabs.weather.search.domain.usecases

import com.maadlabs.weather.search.data.repository.LocationRepoData
import com.maadlabs.weather.search.data.repository.WeatherRepository
import com.maadlabs.weather.search.domain.domain.LocationDomainData
import com.maadlabs.weather.search.domain.domain.WeatherDomainData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal interface GetWeatherForLocationUsecase {
    operator fun invoke(location: LocationDomainData) : Flow<Result<WeatherDomainData>>
}

internal class DefaultGetWeatherForLocationUsecase @Inject constructor(val repository: WeatherRepository)
    : GetWeatherForLocationUsecase {
    override fun invoke(location: LocationDomainData): Flow<Result<WeatherDomainData>> =
        flow {
            emit(repository.getCurrentWeather(
                false,
                LocationRepoData(location.latitude, location.longitude)
            ).toWeatherResult())
        }

}