package com.maadlabs.weather.search.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maadlabs.weather.search.domain.domain.WeatherDomainData
import com.maadlabs.weather.search.domain.domain.WeatherDomainResult
import com.maadlabs.weather.search.domain.interactor.WeatherInteractor
import com.maadlabs.weather.search.ui.model.SearchViewState
import com.maadlabs.weather.search.ui.model.UserEvent
import com.maadlabs.weather.search.ui.model.WeatherScreenData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    app: Application,
    val weatherInteractor: WeatherInteractor
) : AndroidViewModel(app), LocationPermission {

    private val _actions = MutableSharedFlow<Actions>(extraBufferCapacity = 1)
    val actions = _actions.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 0)

    val weatherScreenViewStateFlow: StateFlow<SearchViewState> = weatherInteractor.weatherData
        .onStart {
            weatherInteractor.getDefaultWeather()
        }
        .map {
            when (it) {
                WeatherDomainResult.Default -> SearchViewState.Default
                is WeatherDomainResult.Error -> SearchViewState.Error
                WeatherDomainResult.Loading -> SearchViewState.Loading
                is WeatherDomainResult.RefreshedWeatherData -> SearchViewState.Loaded(it.weatherData.toScreenData())
                is WeatherDomainResult.WeatherData -> SearchViewState.Loaded(it.weatherData.toScreenData())
                is WeatherDomainResult.WeatherDataForLocation -> SearchViewState.Loaded(it.weatherData.toScreenData())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchViewState.Default
        )

    fun userEventsCallback(event: UserEvent) {
        when (event) {
            UserEvent.LocationSearch -> if (isLocationPermissionGranted()) {
                weatherInteractor.getCurrentLocationWeather()
            } else {
                _actions.tryEmit(Actions.CheckLocationPermission)
            }
            UserEvent.Refresh -> weatherInteractor.refreshWeather()
            is UserEvent.Search -> weatherInteractor.getWeather(event.city, true)
        }
    }

    override fun onSuccess() {
        weatherInteractor.getCurrentLocationWeather()
    }

    override fun onFailure() {
    }

    fun isLocationPermissionGranted(): Boolean {
        val context = getApplication<Application>().applicationContext
        return !(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED
            )
    }
}

private fun WeatherDomainData.toScreenData(): WeatherScreenData = WeatherScreenData(
    cityName = city,
    temperature = temperature,
    minTemperature = minTemperature,
    maxTemperature = maxTemperature,
    imageUri = imageUri
)

sealed interface Actions {
    object CheckLocationPermission : Actions
}

interface LocationPermission {
    fun onSuccess()
    fun onFailure()
}
