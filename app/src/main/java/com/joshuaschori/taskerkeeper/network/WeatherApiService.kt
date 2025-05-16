package com.joshuaschori.taskerkeeper.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

interface WeatherApiService {
    @GET("forecast/{apikey}/{latitude},{longitude}")
    fun getForecast(
        @Path("apikey") apikey: String,
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String
    ): Call<ResponseBody>
}