package com.example.myweatherapplication.network

import com.example.myweatherapplication.currentForecastModels.WeatherResponse
import com.example.myweatherapplication.futureForecastModel.FutureWeather
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface WeatherService {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?
    ): Call<WeatherResponse>

    @GET("2.5/weather")
    fun getLocationWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("q") City: String?,
        @Query("appid") appid: String?
    ): Call<WeatherResponse>

    @GET("2.5/forecast")
    fun getFutureWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("q") City: String?,
        @Query("appid") appid: String?

    ): Call<FutureWeather>

}