package com.maadlabs.weather.search.domain.interactor

import com.maadlabs.weather.search.data.repository.LocationRepoData
import com.maadlabs.weather.search.data.repository.LocationRepository
import com.maadlabs.weather.search.data.repository.Search
import com.maadlabs.weather.search.data.repository.SearchRepository
import com.maadlabs.weather.search.domain.domain.DomainError
import com.maadlabs.weather.search.domain.domain.LocationDomainData
import com.maadlabs.weather.search.domain.domain.WeatherDomainResult
import com.maadlabs.weather.search.domain.usecases.GetWeatherForCityUsecase
import com.maadlabs.weather.search.domain.usecases.GetWeatherForLocationUsecase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface WeatherInteractor {
    fun refreshWeather()
    fun getWeather(search: String, useCache: Boolean)
    fun getDefaultWeather()
    val weatherData: Flow<WeatherDomainResult>
    fun getCurrentLocationWeather()
}

internal class DefaultWeatherInteractor @Inject constructor(
    val scope: CoroutineScope,
    val dispatcher: CoroutineDispatcher,
    val cityWeatherUsecase: GetWeatherForCityUsecase,
    val locationWeatherUsecase: GetWeatherForLocationUsecase,
    val searchRepository: SearchRepository,
    val locationRepository: LocationRepository
) : WeatherInteractor {

    private val _citySearch = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val _defaultFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val defaultFlow = _defaultFlow.flatMapLatest { searchRepository.getLastSearchIfPresent() }

    val cityFlow: StateFlow<Search> = _citySearch
        .map { Search.City(it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Search.Unavailable
        )

    private val refreshFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _locationFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val locationFlow = _locationFlow.map {
        locationRepository.getLastLocation()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val _weatherData = merge(
        cityFlow.map { it.toType() },
        locationFlow.map { Type.Location(LocationDomainData(it.latitude, it.longitude)) },
        refreshFlow.map { Type.Refresh },
        defaultFlow.map { it.toType() }
    )
        .flatMapLatest { type ->
            when (type) {
                is Type.City -> {
                    cityWeatherUsecase(type.name, true).map { res ->
                        res.fold({
                            WeatherDomainResult.WeatherData(type.name, it)
                        }, {
                            WeatherDomainResult.Error(DomainError.NETWORK_ERROR)
                        })
                    }.also { searchRepository.saveSearch(type.name) }
                }

                is Type.Location -> {
                    locationWeatherUsecase(type.location).map { res ->
                        res.fold({
                            WeatherDomainResult.WeatherDataForLocation(it)
                        }, {
                            WeatherDomainResult.Error(DomainError.NETWORK_ERROR)
                        })
                    }.also { searchRepository.saveSearch(LocationRepoData(
                        type.location.latitude,
                        type.location.longitude
                    )) }
                }

                Type.Refresh -> {
                    val city = cityFlow.replayCache.first().toType()
                    if (city is Type.City) {
                        cityWeatherUsecase(city.name, false).map { res ->
                            res.fold({
                                WeatherDomainResult.RefreshedWeatherData(city.name, it)
                            }, {
                                WeatherDomainResult.Error(DomainError.NETWORK_ERROR)
                            })
                        }
                    } else {
                        flowOf(WeatherDomainResult.Default)
                    }
                }

                Type.NoAction -> {
                    flowOf(WeatherDomainResult.Default)
                }
            }
        }.flowOn(dispatcher)

    override val weatherData: Flow<WeatherDomainResult>
        get() = _weatherData

    override fun refreshWeather() {
        refreshFlow.tryEmit(Unit)
    }

    override fun getWeather(search: String, useCache: Boolean) {
        _citySearch.tryEmit(search)
    }

    override fun getDefaultWeather() {
        _defaultFlow.tryEmit(Unit)
    }

    override fun getCurrentLocationWeather() {
        _locationFlow.tryEmit(Unit)
    }

    sealed interface Type {
        data class City(val name: String) : Type
        data class Location(val location: LocationDomainData) : Type
        object Refresh : Type
        object NoAction : Type
    }

    private fun Search.toType() =
        when (this) {
            is Search.City -> Type.City(name)
            is Search.Location -> if (location != null) {
                Type.Location(LocationDomainData(location!!.latitude, location!!.longitude))
            } else
                Type.NoAction

            Search.Unavailable -> Type.NoAction
        }
}