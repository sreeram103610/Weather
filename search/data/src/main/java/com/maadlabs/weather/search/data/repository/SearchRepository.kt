package com.maadlabs.weather.search.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SearchRepository {
    fun getLastSearchIfPresent() : Flow<Search>
    suspend fun saveSearch(city: String)
    suspend fun saveSearch(latitude: String, longitude: String)
}

internal class DefaultSearchRepository @Inject constructor(val settingsStore: DataStore<Preferences>): SearchRepository {

    override fun getLastSearchIfPresent() =
        settingsStore.data.map {
            if (it[SEARCH_TYPE].equals(LOCATION_VALUE)) {
                val location =
                    it[LAST_SEARCH]?.split(",")  // Stored in latitude,longitude format
                Search.Location(location?.first().orEmpty(), location?.lastOrNull().orEmpty())
            } else if (it[SEARCH_TYPE].equals(CITY_VALUE))
                Search.City(it[LAST_SEARCH].orEmpty())
            else
                Search.Unavailable
        }.catch { Log.d("DataStore Error: ", it.message.toString()) }

    override suspend fun saveSearch(city: String) {
        settingsStore.edit {
            it[SEARCH_TYPE] = CITY_VALUE
            it[LAST_SEARCH] = city
        }
    }

    override suspend fun saveSearch(latitude: String, longitude: String) {
        settingsStore.edit {
            it[SEARCH_TYPE] = LOCATION_VALUE
            it[LAST_SEARCH] = "$latitude,$longitude"
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
    data class Location(val latitude: String, val longitude: String): Search
    object Unavailable: Search
}

