package com.joshuaschori.taskerkeeper.di

import com.joshuaschori.taskerkeeper.network.WeatherApi
import com.joshuaschori.taskerkeeper.network.WeatherApiImplementation
import com.joshuaschori.taskerkeeper.network.WeatherApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

private const val BASE_URL =
    "https://api.pirateweather.net"

@Module
@InstallIn(SingletonComponent::class)
interface WeatherApiModule {
    companion object {
        @Provides
        @Singleton
        fun provideWeatherApiService(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()
                .create(WeatherApiService::class.java)
        }
    }

    @Binds
    fun bindsWeatherApi(weatherApiImplementation: WeatherApiImplementation): WeatherApi

}