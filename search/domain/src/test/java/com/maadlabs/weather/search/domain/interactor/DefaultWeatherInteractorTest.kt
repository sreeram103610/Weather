package com.maadlabs.weather.search.domain.interactor

import app.cash.turbine.test
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultWeatherInteractorTest {

    private lateinit var closable: AutoCloseable

    @Mock
    private lateinit var locationWeatherUsecase: GetWeatherForLocationUsecase

    @Mock
    private lateinit var cityWeatherUsecase: GetWeatherForCityUsecase

    @Mock
    private lateinit var searchRepo: SearchRepository

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
    fun `test current location weather`() = runTest {
        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11", "12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getCurrentLocationWeather()
            verify(locationWeatherUsecase, times(1)).invoke(LocationDomainData("11", "12"))
            println(awaitItem())
        }
    }

    @Test
    fun `test searched city weather`() = runTest {
        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11", "12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getWeather("Dayton", true)
            verify(cityWeatherUsecase, times(1)).invoke("Dayton", true)
            println(awaitItem())
        }
    }

    @Test
    fun `test get default weather`() = runTest {
        val weatherDomainData = WeatherDomainData("Dayton", "22", "23", "26", "")

        `when`(locationRepo.getLocationUpdates()).thenReturn(flowOf(LocationRepoData("11", "12")))
        `when`(cityWeatherUsecase.invoke("Dayton", true)).thenReturn(flowOf(Result.success(weatherDomainData)))
        `when`(searchRepo.getLastSearchIfPresent()).thenReturn(flowOf(Search.City("Dayton")))
        `when`(locationWeatherUsecase.invoke(LocationDomainData("11", "12"))).thenReturn(flowOf(Result.success(weatherDomainData)))

        interactor.weatherData.test {
            println(awaitItem())
            interactor.getDefaultWeather()
            verify(searchRepo, times(1)).getLastSearchIfPresent()
            println(awaitItem())
        }
    }
}
