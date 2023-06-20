package com.maadlabs.weather.search.data.repository

import android.location.Location
import com.maadlabs.weather.search.data.utils.UserLocationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface LocationRepository {
    suspend fun getLastLocation(): Location
}

internal class DefaultLocationRepository @Inject constructor(val locationManager: UserLocationManager): LocationRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getLastLocation(): Location =
        locationManager.locationFlow().first()

}



