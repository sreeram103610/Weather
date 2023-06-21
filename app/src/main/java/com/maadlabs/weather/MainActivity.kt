package com.maadlabs.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.maadlabs.weather.search.ui.viewmodel.Actions
import com.maadlabs.weather.search.ui.viewmodel.LocationPermission
import com.maadlabs.weather.search.ui.viewmodel.SearchViewModel
import com.maadlabs.weather.search.ui.views.Views
import com.maadlabs.weather.ui.theme.WeatherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val searchViewModel: SearchViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTheme {
                // A surface container using the 'background' color from the theme
                Views.SearchView(
                    consumer = searchViewModel::userEventsCallback,
                    state = searchViewModel.weatherScreenViewStateFlow
                )
            }
        }

        lifecycleScope.launch {
            searchViewModel.actions.collect {
                when(it) {
                    Actions.CheckLocationPermission -> startLocationPermissionRequest()
                }
            }
        }

    }

    val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if(!(searchViewModel is LocationPermission))
            return@registerForActivityResult
        if(permissions.entries.map { it.value }.reduce{ acc, item -> acc && item})
            (searchViewModel as LocationPermission).onSuccess()
        else
            (searchViewModel as LocationPermission).onFailure()
    }

    private fun startLocationPermissionRequest() {
        requestMultiplePermissions.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

}
