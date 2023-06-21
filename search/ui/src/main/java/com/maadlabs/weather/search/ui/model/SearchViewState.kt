package com.maadlabs.weather.search.ui.model

sealed interface SearchViewState {
    object Loading: SearchViewState
    object Error: SearchViewState
    object Default: SearchViewState
    data class Loaded(val data: WeatherScreenData): SearchViewState
}

sealed interface UserEvent {
    object Refresh: UserEvent
    data class Search(val city: String): UserEvent
    object LocationSearch: UserEvent
}