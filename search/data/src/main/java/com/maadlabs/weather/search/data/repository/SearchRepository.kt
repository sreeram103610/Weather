package com.maadlabs.weather.search.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface SearchRepository {
    fun getLastSearchIfPresent(): Flow<Search>
    suspend fun saveSearch(city: String)
    suspend fun saveSearch(locationRepoData: LocationRepoData)
}

internal class DefaultSearchRepository @Inject constructor(val settingsStore: DataStore<Preferences>) : SearchRepository {

    val locationMoshi = Moshi.Builder().build().adapter(LocationRepoData::class.java)

    override fun getLastSearchIfPresent(): Flow<Search> {
        return settingsStore.data.map {
            if (it[SEARCH_TYPE].equals(LOCATION_VALUE)) {
                it[LAST_SEARCH].let { location ->
                    if (location != null) {
                        Search.Location(locationMoshi.fromJson(location))
                    } else {
                        Search.Unavailable
                    }
                }
            } else if (it[SEARCH_TYPE].equals(CITY_VALUE)) {
                Search.City(it[LAST_SEARCH].orEmpty())
            } else {
                Search.Unavailable
            }
        }
    }

    override suspend fun saveSearch(city: String) {
        settingsStore.edit {
            it[SEARCH_TYPE] = CITY_VALUE
            it[LAST_SEARCH] = city
        }
    }

    override suspend fun saveSearch(locationRepoData: LocationRepoData) {
        settingsStore.edit {
            it[SEARCH_TYPE] = LOCATION_VALUE
            it[LAST_SEARCH] = locationMoshi.toJson(locationRepoData)
        }
    }

    internal companion object {
        val LAST_SEARCH = stringPreferencesKey("last_search")
        val SEARCH_TYPE = stringPreferencesKey("search_type")

        const val CITY_VALUE = "city_value"
        const val LOCATION_VALUE = "location_value"
    }
}

sealed interface Search {
    data class City(val name: String) : Search
    data class Location(val location: LocationRepoData?) : Search
    object Unavailable : Search
}
