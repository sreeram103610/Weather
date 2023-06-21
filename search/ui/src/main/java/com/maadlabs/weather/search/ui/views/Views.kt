package com.maadlabs.weather.search.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.maadlabs.weather.search.ui.R
import com.maadlabs.weather.search.ui.model.SearchViewState
import com.maadlabs.weather.search.ui.model.UserEvent
import com.maadlabs.weather.search.ui.model.WeatherScreenData
import com.maadlabs.weather.search.ui.views.Views.SearchView
import com.maadlabs.weather.search.ui.views.Views.WeatherDetailsView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Views {

    object TestTags {
        const val SEARCHVIEW_WEATHER_DETAILS = "WeatherDetailsView"
        const val SEARCHVIEW_TEMPERATURE = "Temperature"
        const val SEARCHVIEW_TEMPERATURE_HIGH = "TemperatureHigh"
        const val SEARCHVIEW_TEMPERATURE_LOW = "TemperatureLow"
        const val SEARCHVIEW_CITY_NAME = "CityName"
    }

    @Composable
    fun SearchView(consumer: (UserEvent) -> Unit, state: StateFlow<SearchViewState>) {
        val viewState by state.collectAsStateWithLifecycle()
        var cityName by rememberSaveable { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text(stringResource(R.string.city), fontSize = 18.sp) },
                    textStyle = TextStyle(fontSize = 24.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                IconButton(onClick = { if (cityName.isNotBlank()) consumer(UserEvent.Search(cityName)) }) {
                    Icon(imageVector = Icons.Default.Search, stringResource(R.string.search))
                }
                IconButton(onClick = { consumer(UserEvent.LocationSearch) }) {
                    Icon(imageVector = Icons.Default.LocationOn, stringResource(R.string.location))
                }
            }

            when (viewState) {
                is SearchViewState.Error -> ErrorView(consumer)
                is SearchViewState.Loaded -> {
                    val screendata = (viewState as SearchViewState.Loaded).data
                    WeatherDetailsView(weatherScreenData = screendata, consumer = consumer)
                    LaunchedEffect(key1 = screendata) {
                        cityName = screendata.cityName
                    }
                }
                is SearchViewState.Loading -> CircularProgressIndicator(modifier = Modifier.size(64.dp))
                else -> {}
            }
        }
    }

    @Composable
    fun ErrorView(consumer: (UserEvent) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentHeight()) {
            Text(text = stringResource(R.string.unknown_error_occured))
            Button(
                onClick = { consumer(UserEvent.Refresh) },
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(4.dp)
            ) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun WeatherDetailsView(weatherScreenData: WeatherScreenData, consumer: (UserEvent) -> Unit) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = { },
                    onLongClick = { consumer(UserEvent.Refresh) }
                )
                .padding(top = 16.dp, start = 16.dp)
                .fillMaxWidth()
                .testTag(TestTags.SEARCHVIEW_WEATHER_DETAILS),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Text(text = weatherScreenData.cityName, fontSize = 32.sp, fontStyle = FontStyle.Italic, modifier = Modifier.testTag(TestTags.SEARCHVIEW_CITY_NAME))
                Text(text = weatherScreenData.temperature, fontSize = 64.sp, modifier = Modifier.testTag(TestTags.SEARCHVIEW_TEMPERATURE))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(weatherScreenData.imageUri)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(id = R.string.description),
                    modifier = Modifier
                        .widthIn(64.dp, 128.dp)
                        .heightIn(64.dp, 128.dp)
                )
            }
            Column {
                Text(text = stringResource(R.string.low))
                Text(text = weatherScreenData.minTemperature, fontSize = 64.sp, modifier = Modifier.testTag(TestTags.SEARCHVIEW_TEMPERATURE_LOW))
                Text(text = stringResource(R.string.high))
                Text(text = weatherScreenData.maxTemperature, fontSize = 64.sp, modifier = Modifier.testTag(TestTags.SEARCHVIEW_TEMPERATURE_HIGH))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewSearchView() {
    val data = WeatherScreenData("34", "30", "44", "https://openweathermap.org/img/wn/01d@2x.png", "Houston")

    SearchView({}, MutableStateFlow(SearchViewState.Loaded(data)))
}
