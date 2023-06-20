package com.maadlabs.weather.search.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)  // TODO
class DefaultSearchRepositoryTest {
    private lateinit var repository: DefaultSearchRepository
    private val testContext: Context = ApplicationProvider.getApplicationContext()

    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { testContext.preferencesDataStoreFile("test") }
        )

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        repository = DefaultSearchRepository(testDataStore)
    }

    @After
    fun teardown() {
        closeable.close()
    }

    @Test
    fun `get city info`() = runTest {
        repository.getLastSearchIfPresent().collect {
            assertThat(it).isEqualTo(Search.Unavailable)
        }
    }
}