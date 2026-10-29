
package au.edu.jcu.assessment.utilityapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code",
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    companion object {
        fun create(): WeatherApi {
            return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApi::class.java)
        }
    }
}


data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?
)

interface GeocodingApi {

    @retrofit2.http.GET("v1/search")
    suspend fun searchCity(
        @retrofit2.http.Query("name") name: String,
        @retrofit2.http.Query("count") count: Int = 5,
        @retrofit2.http.Query("language") language: String = "en",
        @retrofit2.http.Query("format") format: String = "json"
    ): GeocodingResponse

    companion object {
        fun create(): GeocodingApi {
            return retrofit2.Retrofit.Builder()
                .baseUrl("https://geocoding-api.open-meteo.com/")
                .addConverterFactory(
                    retrofit2.converter.gson.GsonConverterFactory.create()
                )
                .build()
                .create(GeocodingApi::class.java)
        }
    }
}

