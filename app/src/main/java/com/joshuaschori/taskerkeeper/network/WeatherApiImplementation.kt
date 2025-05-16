package com.joshuaschori.taskerkeeper.network

import com.joshuaschori.taskerkeeper.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

const val apikey = BuildConfig.API_KEY

@Singleton
class WeatherApiImplementation @Inject constructor(
    private val retrofitService: WeatherApiService
): WeatherApi {
    override fun getForecast(latitude: String, longitude: String): String {
        val call = retrofitService.getForecast(
            apikey = apikey,
            latitude = latitude,
            longitude = longitude
        )
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return body.string()
            } else {
                // TODO error handling
                return "null body"
            }
        } else {
            // TODO better error handling
            return "response not successful"
        }
    }
}