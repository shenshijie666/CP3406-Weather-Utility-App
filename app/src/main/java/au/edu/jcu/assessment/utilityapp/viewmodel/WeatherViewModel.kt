
package au.edu.jcu.assessment.utilityapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.WeatherResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather: StateFlow<WeatherResponse?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _city = MutableStateFlow("Singapore")
    val city: StateFlow<String> = _city.asStateFlow()

    fun searchWeather(cityName: String) {
        val requestedCity = cityName.trim()

        if (requestedCity.isBlank()) {
            _weather.value = null
            _error.value = "Please enter a city name."
            return
        }

        if (_isLoading.value) return

        _city.value = requestedCity
        loadWeatherForCity(requestedCity)
    }

    fun loadWeather() {
        searchWeather(_city.value)
    }

    private fun loadWeatherForCity(cityName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Clear previous weather before searching.
            _weather.value = null

            try {
                val result = repository.getWeatherForCity(cityName)

                // Update weather only after a successful response.
                _weather.value = result
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _weather.value = null
                _error.value = e.localizedMessage
                    ?: "Unable to load weather. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
