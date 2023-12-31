package com.maadlabs.weather.search.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.maadlabs.weather.search.data.BuildConfig
import com.maadlabs.weather.search.data.api.WeatherApi
import com.maadlabs.weather.search.data.api.adapter.DataResultCallAdapterFactory
import com.maadlabs.weather.search.data.di.DataModule.Constants.APPID_KEY
import com.maadlabs.weather.search.data.di.DataModule.Constants.CACHE_CONTROL
import com.maadlabs.weather.search.data.di.DataModule.Constants.CACHE_DIR_NAME
import com.maadlabs.weather.search.data.di.DataModule.Constants.CACHE_SIZE
import com.maadlabs.weather.search.data.di.DataModule.Constants.SETTINGS
import com.maadlabs.weather.search.data.di.DataModule.Constants.UNITS_KEY
import com.maadlabs.weather.search.data.di.DataModule.Constants.UNITS_VALUE
import com.maadlabs.weather.search.data.repository.DefaultLocationRepository
import com.maadlabs.weather.search.data.repository.DefaultSearchRepository
import com.maadlabs.weather.search.data.repository.DefaultWeatherRepository
import com.maadlabs.weather.search.data.repository.LocationRepository
import com.maadlabs.weather.search.data.repository.SearchRepository
import com.maadlabs.weather.search.data.repository.WeatherRepository
import com.maadlabs.weather.search.data.utils.UserLocationManager
import com.maadlabs.weather.search.data.utils.Utils
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes



@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Singleton
    internal fun weatherRepo(weatherApi: WeatherApi): WeatherRepository = DefaultWeatherRepository(weatherApi)

    @Provides
    @Singleton
    internal fun searchRepo(settings: DataStore<Preferences>): SearchRepository = DefaultSearchRepository(settings)

    @Provides
    @Singleton
    internal fun locationManager(@ApplicationContext context: Context) = UserLocationManager(context, CoroutineScope(Dispatchers.IO))

    @Provides
    @Singleton
    internal fun locationRepo(locationManager: UserLocationManager): LocationRepository = DefaultLocationRepository(locationManager)

    @Provides
    @Singleton
    internal fun moshiProvider() = Moshi.Builder().build()

    @Provides
    @Singleton
    internal fun retrofitProvider(client: OkHttpClient) =
        Retrofit.Builder()
            .baseUrl(BuildConfig.WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(DataResultCallAdapterFactory())
            .build()

    @Provides
    @Singleton
    internal fun weatherApiProvider(retrofit: Retrofit) = retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    internal fun okhttpProvider(
        @ApplicationContext context: Context,
        @Named("CachingInterceptor") cacheInterceptor: Interceptor,
        @Named("ApiInterceptor") apiInterceptor: Interceptor
    ) =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            .cache(Cache(directory = File(context.cacheDir, CACHE_DIR_NAME), maxSize = CACHE_SIZE))
            .addInterceptor(apiInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("ApiInterceptor")
    internal fun apiInterceptorProvider() = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original: Request = chain.request()
            val originalHttpUrl: HttpUrl = original.url

            val url = originalHttpUrl.newBuilder()
                .addQueryParameter(APPID_KEY, BuildConfig.WEATHER_APP_KEY)
                .addQueryParameter(UNITS_KEY, UNITS_VALUE)
                .build()
            val requestBuilder: Request.Builder = original.newBuilder()
                .url(url)

            val request: Request = requestBuilder.build()
            return chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("CachingInterceptor")
    internal fun cacheInterceptorProvider(@ApplicationContext context: Context) = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            if (Utils.isInternetAvailable(context)) {
                val cacheDuration = 15.minutes.inWholeSeconds
                return response.newBuilder().header(
                    CACHE_CONTROL,
                    "public, max-age=$cacheDuration"
                ).removeHeader("pragma")
                    .build()
            } else {
                val maxStale = 60.minutes.inWholeSeconds
                return response.newBuilder()
                    .header(CACHE_CONTROL, "public, only-if-cached, max-stale=$maxStale")
                    .build()
            }
        }
    }

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(SETTINGS) }
        )
    }

    object Constants {
        const val CACHE_CONTROL = "Cache-Control"

        const val SETTINGS = "settings"

        const val APPID_KEY = "appid"

        const val UNITS_KEY = "units"

        const val UNITS_VALUE = "imperial"

        const val CACHE_DIR_NAME = "okhttp_cache"

        const val CACHE_SIZE = 10 * 1024 * 1024L
    }
}
