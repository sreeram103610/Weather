package com.maadlabs.weather.search.domain.di

import com.maadlabs.weather.search.data.repository.LocationRepository
import com.maadlabs.weather.search.data.repository.SearchRepository
import com.maadlabs.weather.search.data.repository.WeatherRepository
import com.maadlabs.weather.search.domain.interactor.DefaultWeatherInteractor
import com.maadlabs.weather.search.domain.interactor.WeatherInteractor
import com.maadlabs.weather.search.domain.usecases.DefaultGetWeatherForCityUsecase
import com.maadlabs.weather.search.domain.usecases.DefaultGetWeatherForLocationUsecase
import com.maadlabs.weather.search.domain.usecases.GetWeatherForCityUsecase
import com.maadlabs.weather.search.domain.usecases.GetWeatherForLocationUsecase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DomainModule {

    @Provides
    @Singleton
    internal fun dispatcherProvider() = Dispatchers.IO

    @Provides
    @Singleton
    internal fun cityWeatherUsecaseProvider(repo: WeatherRepository): GetWeatherForCityUsecase = DefaultGetWeatherForCityUsecase(repo)

    @Provides
    @Singleton
    internal fun locationWeatherUsecaseProvider(repo: WeatherRepository): GetWeatherForLocationUsecase = DefaultGetWeatherForLocationUsecase(repo)

    @Provides
    internal fun weatherInteractorProvider(
        dispatcher: CoroutineDispatcher,
        cityWeatherUsecase: GetWeatherForCityUsecase,
        locationWeatherUsecase: GetWeatherForLocationUsecase,
        searchRepository: SearchRepository,
        locationRepository: LocationRepository
    ): WeatherInteractor {
        val defaultWeatherInteractor = DefaultWeatherInteractor(
            CoroutineScope(Job() + Dispatchers.IO),
            dispatcher,
            cityWeatherUsecase,
            locationWeatherUsecase,
            searchRepository,
            locationRepository
        )
        return defaultWeatherInteractor
    }
}
