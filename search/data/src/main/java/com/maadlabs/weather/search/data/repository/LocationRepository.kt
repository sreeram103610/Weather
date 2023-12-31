package com.maadlabs.weather.search.data.repository

import com.maadlabs.weather.search.data.utils.UserLocationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Get location information for the user's
 * current location
 */
interface LocationRepository {
    suspend fun getLocationUpdates(): Flow<LocationRepoData>
}

internal class DefaultLocationRepository @Inject constructor(val locationManager: UserLocationManager) : LocationRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getLocationUpdates(): Flow<LocationRepoData> =
        locationManager.locationFlow().map {
            LocationRepoData(it.latitude.toString(), it.longitude.toString())
        }
}
