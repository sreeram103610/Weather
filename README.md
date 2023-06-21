# Weather
Sample weather api for openweatherapi

## Description
- Written in MVVM based on a clean architecture design pattern
- Uses offline caching for network requests for both images and text
- Remember last searched user location and weather information

### Modules
:app
:search
:search:ui
:search:domain
:search:data

### Tech Stack
Hilt, Jetpack Compose, OkHTTP, retrofit, coroutines, Kotlin Flow, Coil, mockito, Truth

## How to Use
- Use the search bar to search for city
- Use the location icon to get user's weather for current location
- Long press on weather screen to get latest data, instead of cached data

Cache Duration is 15 minutes for new network requests. Offline Cache duration
is 1 hour after which you'll get a error.


