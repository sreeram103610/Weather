package com.maadlabs.weather.search.domain.usecases

import com.maadlabs.weather.search.data.repository.RepoErrorType
import com.maadlabs.weather.search.data.repository.RepoResult
import com.maadlabs.weather.search.data.repository.WeatherRepoData
import com.maadlabs.weather.search.data.repository.WeatherRepository
import com.maadlabs.weather.search.domain.domain.WeatherDomainData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

internal interface GetWeatherForCityUsecase {
    operator fun invoke(search: String, useCache: Boolean): Flow<Result<WeatherDomainData>>
}

internal class DefaultGetWeatherForCityUsecase @Inject constructor(val repository: WeatherRepository): GetWeatherForCityUsecase {

    override fun invoke(search: String, useCache: Boolean): Flow<Result<WeatherDomainData>> =
        flow<Result<WeatherDomainData>> {
            emit(repository.getCurrentWeather(useCache, search).toWeatherResult())
        }.apply {
            onEach {
            println("REPO DATA - $it")
         }
        }

}

fun <E : RepoErrorType> RepoResult<WeatherRepoData, E>.toWeatherResult(): Result<WeatherDomainData> {
    return when(this) {
        is RepoResult.Available -> Result.success(this.repoData.toWeatherDomainData())
        is RepoResult.NotAvailable -> Result.failure(Throwable(this.error.toString()))
    }
}

private fun WeatherRepoData.toWeatherDomainData(): WeatherDomainData {
    return WeatherDomainData(city = this.locationName, minTemperature = minTemperature, maxTemperature = maxTemperature, temperature = temperature, imageUri = imageUri)
}
