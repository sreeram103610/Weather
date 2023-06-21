package com.maadlabs.weather.search.ui

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.maadlabs.weather.search.ui.model.SearchViewState
import com.maadlabs.weather.search.ui.model.WeatherScreenData
import com.maadlabs.weather.search.ui.views.Views
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearch() {
        val data = WeatherScreenData("33", "1", "50", "uri:", "houston")
        val flow = MutableStateFlow(SearchViewState.Loaded(data))
        composeTestRule.setContent {
            Views.SearchView(consumer = {}, state = flow)
        }

        composeTestRule.onNodeWithTag(Views.TestTags.SEARCHVIEW_TEMPERATURE, useUnmergedTree = true).assertTextEquals(data.temperature + "F")
        composeTestRule.onNodeWithTag(Views.TestTags.SEARCHVIEW_TEMPERATURE_HIGH, useUnmergedTree = true).assertTextEquals(data.maxTemperature + "F")
        composeTestRule.onNodeWithTag(Views.TestTags.SEARCHVIEW_TEMPERATURE_LOW, useUnmergedTree = true).assertTextEquals(data.minTemperature + "F")
    }
}
