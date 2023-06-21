package com.maadlabs.weather.search.data

import com.google.common.truth.Truth.assertThat
import com.maadlabs.weather.search.data.api.WeatherApi
import com.maadlabs.weather.search.data.api.adapter.DataResultCallAdapterFactory
import com.maadlabs.weather.search.data.dto.ConditionData
import com.maadlabs.weather.search.data.dto.MainSourceData
import com.maadlabs.weather.search.data.dto.WeatherSourceData
import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WeatherApiTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var weatherApi: WeatherApi
    private val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()).build()

    @Before
    fun setup() {
        val jsonConverter = MoshiConverterFactory.create()
        mockWebServer = MockWebServer()
        weatherApi = Retrofit.Builder().baseUrl(mockWebServer.url("/")).client(client)
            .addConverterFactory(jsonConverter)
            .addCallAdapterFactory(DataResultCallAdapterFactory())
            .build()
            .create(WeatherApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `400 should return network error state`() = runTest {
        val response = MockResponse()
            .setResponseCode(400)
            .setBody("Client side error")

        mockWebServer.enqueue(response)
        launch {
            val res = weatherApi.currentWeather("any")
            assertThat(res).isInstanceOf(DataResult.Error::class.java)
            assertThat((res as DataResult.Error).errorType).isInstanceOf(ErrorResponse.NetworkError::class.java)
        }
    }

    @Test
    fun `malformed json is parsed and error is produced`() = runTest {
        val jsonString = """
            {
                "coord": {
                    "lon": -84.1916,
                    "lat": 39.7589
                },
                "weather": [
                    {
                        "id": 701,
                        "main": "Mist",
                        "description": "mist",
                        "icon": "50d"
                    }
                ],
                "base": "stations",
                "main": {
                    "temp": 294.19,
                    "feels_like": 294.65,
                    "temp_min": 293.71,
                    "temp_max": 295.35,
                    "pressure": 1011,
                    "humidity": 88
                },
                "visibility": 10000,
                "wind": {
                    "speed": 3.6,
                    "deg": 30
                },
                "clouds": {
                    "all": 100
                },
                "dt": 1687217114,
                "sys": {
                    "type": 1,
                    "id": 4087,
                    "country": "US",
                    "sunrise": 1687169310,
                    "sunset": 1687223259
                },
                "timezone": -14400,
                "id": 4509884,
                "cod": 200
            }
        """.trimIndent()

        val response = MockResponse()
            .setResponseCode(200)
            .setBody(jsonString)

        mockWebServer.enqueue(response)
        launch {
            val res = weatherApi.currentWeather("any")
            assertThat(res).isInstanceOf(DataResult.Error::class.java)
        }
    }

    @Test
    fun `weather json is parsed correctly`() = runTest {
        val jsonString = """
            {"coord":{"lon":-96.7836,"lat":32.7668},"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"base":"stations","main":{"temp":306.86,"feels_like":313.86,"temp_min":305.29,"temp_max":308.56,"pressure":1007,"humidity":68},"visibility":10000,"wind":{"speed":4.12,"deg":120},"clouds":{"all":0},"dt":1687309352,"sys":{"type":2,"id":2075302,"country":"US","sunrise":1687259962,"sunset":1687311477},"timezone":-18000,"id":4684904,"name":"Dallas","cod":200}
        """.trimIndent()
        val response = MockResponse()
            .setResponseCode(200)
            .setBody(jsonString)
        val serializedSourceData = WeatherSourceData("Dallas", MainSourceData(306.86f, 305.29f, 308.56f), listOf(ConditionData("01d", "Clear")))

        mockWebServer.enqueue(response)
        launch {
            val res = weatherApi.currentWeather("any")
            assertThat(res).isInstanceOf(DataResult.Success::class.java)
            assertThat((res as DataResult.Success).data).isEqualTo(serializedSourceData)
        }
    }
}
