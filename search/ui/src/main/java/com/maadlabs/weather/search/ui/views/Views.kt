package com.maadlabs.weather.search.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
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
import kotlinx.coroutines.flow.flowOf

object Views {

    @Composable
    fun SearchView(consumer: (UserEvent) -> Unit, state: StateFlow<SearchViewState>) {

        val viewState by state.collectAsStateWithLifecycle()
        var cityName by rememberSaveable { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {


                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("City", fontSize = 18.sp) },
                    textStyle = TextStyle(fontSize = 24.sp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                IconButton(onClick =  { if(cityName.isNotBlank()) consumer(UserEvent.Search(cityName)) }) {
                    Icon(imageVector = Icons.Default.Search, "Search")
                }
                IconButton(onClick =  { consumer(UserEvent.LocationSearch) }) {
                    Icon(imageVector = Icons.Default.LocationOn, "Location")
                }
            }

            when (viewState) {
                is SearchViewState.Error -> ErrorView(consumer)
                is SearchViewState.Loaded -> {
                    val screendata = (viewState as SearchViewState.Loaded).data
                    WeatherDetailsView(weatherScreenData = screendata)
                }
                is SearchViewState.Loading -> CircularProgressIndicator(modifier = Modifier.size(64.dp))
                else -> {}
            }
        }
    }

    @Composable
    fun ErrorView(consumer: (UserEvent) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentHeight()) {
            Text(text = "Unknown Error Occured",)
            Button(onClick = { consumer(UserEvent.Refresh) }, modifier = Modifier
                .wrapContentHeight()
                .padding(4.dp)) {
                Text(text = "RETRY")
            }
        }
    }

    @Composable
    fun WeatherDetailsView(weatherScreenData: WeatherScreenData) {

        Row(modifier = Modifier
            .padding(top = 16.dp, start = 16.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column {
                Text(text = weatherScreenData.temperature, fontSize = 64.sp)
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
                Text(text = weatherScreenData.cityName, fontSize = 32.sp, fontStyle = FontStyle.Italic)
                Text(text = "Low")
                Text(text = weatherScreenData.minTemperature, fontSize = 64.sp)
                Text(text = "High")
                Text(text = weatherScreenData.maxTemperature, fontSize = 64.sp)
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