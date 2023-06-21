package com.maadlabs.weather.search.domain.interactor

import android.location.Location
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.maadlabs.weather.search.data.repository.LocationRepoData
import com.maadlabs.weather.search.data.repository.LocationRepository
import com.maadlabs.weather.search.data.repository.Search
import com.maadlabs.weather.search.data.repository.SearchRepository
import com.maadlabs.weather.search.domain.domain.LocationDomainData
import com.maadlabs.weather.search.domain.domain.WeatherDomainData
import com.maadlabs.weather.search.domain.usecases.GetWeatherForCityUsecase
import com.maadlabs.weather.search.domain.usecases.GetWeatherForLocationUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.Closeable

@RunWith(MockitoJUnitRunner::class)
class DefaultWeatherInteractorTest {

    private lateinit var closable: AutoCloseable

    @Mock
    private lateinit var locationWeatherUsecase: GetWeatherForLocationUsecase

    @Mock
    private lateinit var cityWeatherUsecase: GetWeatherForCityUsecase

    @Mock
    private lateinit var searchRepo : SearchRepository

    @Mock
    private lateinit var locationRepo: LocationRepository

    private lateinit var interactor: DefaultWeatherInteractor

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        interactor = DefaultWeatherInteractor(
            scope = CoroutineScope(Dispatchers.Unconfined),
            dispatcher = UnconfinedTestDispatcher(),
            locationRepository = locationRepo,
            cityWeatherUsecase = cityWeatherUsecase,
            searchRepository = searchRepo,
            locationWeatherUsecase = locationWeatherUsecase
        )
    }

    @After
    fun teardown() {
        closable.close()
    }

    @Test
    fun `test current location weather`() = runTest{

        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11","12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getCurrentLocationWeather()
            verify(locationWeatherUsecase, times(1)).invoke(LocationDomainData("11","12"))
            println(awaitItem())
        }
    }

    @Test
    fun `test searched city weather`() = runTest{

        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11","12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getWeather("Dayton", true)
            verify(cityWeatherUsecase, times(1)).invoke("Dayton", true)
            println(awaitItem())
        }
    }

    @Test
    fun `test get default weather`() = runTest{

        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11","12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getDefaultWeather()
            verify(searchRepo, times(1)).getLastSearchIfPresent()
            println(awaitItem())
        }
    }


}