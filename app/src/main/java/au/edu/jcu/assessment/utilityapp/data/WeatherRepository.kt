

package au.edu.jcu.assessment.utilityapp.data

class WeatherRepository(
    private val weatherApi: WeatherApi = WeatherApi.create(),
    private val geocodingApi: GeocodingApi = GeocodingApi.create()
) {
    suspend fun getWeatherForCity(city: String): WeatherResponse {
        val cityName = city.trim()

        if (cityName.isBlank()) {
            throw IllegalArgumentException("Please enter a city name.")
        }

// Reject numeric-only input such as 1111
        if (cityName.all { it.isDigit() }) {
            throw IllegalArgumentException(
                "Invalid city name. Please enter a valid city."
            )
        }

        val response = geocodingApi.searchCity(cityName)
        val results = response.results

        if (results.isNullOrEmpty()) {
            throw IllegalArgumentException(
                "City \"$cityName\" not found. Please enter a valid city."
            )
        }

        val location = results.firstOrNull()
            ?: throw IllegalArgumentException("City not found.")

        return weatherApi.getWeather(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }
}
