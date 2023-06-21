package com.maadlabs.weather.search.data.repository

import com.google.common.truth.Truth.assertThat
import com.maadlabs.weather.search.data.api.WeatherApi
import com.maadlabs.weather.search.data.dto.ConditionData
import com.maadlabs.weather.search.data.dto.MainSourceData
import com.maadlabs.weather.search.data.dto.WeatherSourceData
import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultWeatherRepositoryTest {

    lateinit var repository: WeatherRepository

    @Mock
    private lateinit var mockWeatherApi: WeatherApi

    @Before
    fun setup() {
        mockWeatherApi = mock(WeatherApi::class.java)
        repository = DefaultWeatherRepository(mockWeatherApi)
    }

    @Test
    fun `check if successful response gives successful repo status`() = runTest {
        val data = WeatherSourceData("Dayton", MainSourceData(1f, 2f, 3f), listOf(ConditionData("", "")))
        val dataRepo = WeatherRepoData("Dayton", "1", "2", "3", "")
        val response = DataResult.Success(data)
        `when`(mockWeatherApi.currentWeatherNoCache("")).thenReturn(response)
        assertThat(repository.getCurrentWeather(false, "")).isEqualTo(RepoResult.Available(dataRepo))
    }

    @Test
    fun `check if error response gives error repo status`() = runTest {
        val response = DataResult.Error(ErrorResponse.NetworkError)
        `when`(mockWeatherApi.currentWeatherNoCache("")).thenReturn(response)
        assertThat(repository.getCurrentWeather(false, "")).isEqualTo(RepoResult.NotAvailable<RepoErrorType>(RepoErrorType.NetworkError))
    }
}
