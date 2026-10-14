
package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.data.CurrentWeather
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import au.edu.jcu.assessment.utilityapp.viewmodel.WeatherViewModel
import au.edu.jcu.assessment.utilityapp.viewmodel.WeatherViewModelFactory
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobileTech_UtilityAppTheme {
                UtilityApp()
            }
        }
    }
}

@Composable
fun UtilityApp() {
    var selectedTab by remember { mutableStateOf("Utility") }
    var cityInput by remember { mutableStateOf("Singapore") }
    var isCelsius by remember { mutableStateOf(true) }

    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory()
    )

    val weather by weatherViewModel.weather.collectAsState()
    val isLoading by weatherViewModel.isLoading.collectAsState()
    val error by weatherViewModel.error.collectAsState()
    val currentCity by weatherViewModel.city.collectAsState()

    LaunchedEffect(Unit) {
        weatherViewModel.loadWeather()
    }

    // Switch to the weather screen only after a successful search.
    LaunchedEffect(isLoading, error, weather) {
        if (!isLoading && error == null && weather != null) {
            selectedTab = "Utility"
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.Home,
                            contentDescription = "Weather"
                        )
                    },
                    label = { Text("Weather") },
                    selected = selectedTab == "Utility",
                    onClick = { selectedTab = "Utility" }
                )

                NavigationBarItem(
                    icon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Utility" -> UtilityScreen(
                    city = currentCity,
                    isCelsius = isCelsius,
                    current = weather?.current,
                    hourlyTemperatures =
                        weather?.hourly?.temperatures.orEmpty(),
                    isLoading = isLoading,
                    error = error,
                    onRefresh = {
                        weatherViewModel.loadWeather()
                    }
                )

                "Settings" -> SettingsScreen(
                    city = cityInput,
                    onCityChange = { cityInput = it },
                    isCelsius = isCelsius,
                    onUnitChange = { isCelsius = it },
                    isLoading = isLoading,
                    error = error,
                    onSearch = {
                        weatherViewModel.searchWeather(cityInput)
                    }
                )
            }
        }
    }
}

@Composable
fun UtilityScreen(
    city: String,
    isCelsius: Boolean,
    current: CurrentWeather?,
    hourlyTemperatures: List<Double>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "WEATHER TODAY",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = city,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            CircularProgressIndicator()
            Text("Loading weather data...")
        }

        if (error != null) {
            Text(
                text = "Unable to load weather: $error",
                color = MaterialTheme.colorScheme.error
            )

            Button(onClick = onRefresh) {
                Text("Try again")
            }
        }

        if (current != null) {
            Text(
                text = weatherDescription(current.weatherCode),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = formatTemperature(
                    current.temperature, isCelsius
                ),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light
            )

            Text(
                text = "Feels like ${
                    formatTemperature(
                        current.apparentTemperature, isCelsius
                    )
                }"
            )

            Text(
                text = "Weather details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherDetailCard(
                    title = "Humidity",
                    value = "${current.humidity}%",
                    modifier = Modifier.weight(1f)
                )

                WeatherDetailCard(
                    title = "Wind speed",
                    value = "${current.windSpeed.roundToInt()} km/h",
                    modifier = Modifier.weight(1f)
                )
            }

            val high = hourlyTemperatures.maxOrNull()
            val low = hourlyTemperatures.minOrNull()

            if (high != null && low != null) {
                WeatherDetailCard(
                    title = "Today's forecast high / low",
                    value = "${formatTemperature(high, isCelsius)} / ${
                        formatTemperature(low, isCelsius)
                    }",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Refresh,
                    contentDescription = null
                )
                Text(
                    if (isLoading) "Refreshing..." else "Refresh weather",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = "Weather data provided by Open-Meteo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeatherDetailCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SettingsScreen(
    city: String,
    onCityChange: (String) -> Unit,
    isCelsius: Boolean,
    onUnitChange: (Boolean) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Weather Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            label = { Text("City name") },
            placeholder = {
                Text("e.g. Tokyo, London, Singapore")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSearch,
            enabled = !isLoading && city.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Searching..." else "Search weather"
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Temperature unit",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (isCelsius) "Celsius (°C)" else "Fahrenheit (°F)"
            )

            Switch(
                checked = isCelsius,
                onCheckedChange = onUnitChange
            )
        }

        Text(
            text = "Weather data is retrieved from Open-Meteo.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTemperature(
    celsius: Double,
    isCelsius: Boolean
): String {
    val value = if (isCelsius) {
        celsius
    } else {
        celsius * 9.0 / 5.0 + 32.0
    }

    val unit = if (isCelsius) "°C" else "°F"
    return "${value.roundToInt()}$unit"
}

private fun weatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        in 51..57 -> "Drizzle"
        in 61..67 -> "Rain"
        in 71..77 -> "Snow"
        in 80..82 -> "Rain showers"
        in 85..86 -> "Snow showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Weather conditions unavailable"
    }
}
