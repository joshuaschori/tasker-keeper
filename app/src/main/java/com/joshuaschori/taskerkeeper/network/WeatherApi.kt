package com.joshuaschori.taskerkeeper.network

import javax.inject.Inject
import javax.inject.Singleton

interface WeatherApi {
    fun getForecast(latitude: String, longitude: String): String
}