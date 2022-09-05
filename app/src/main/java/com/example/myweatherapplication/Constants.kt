package com.example.myweatherapplication

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants {

    const val APP_ID : String = "74c28959aa5ae277c811f327c112d3ed"
    const val BASE_URL: String = "http://api.openweathermap.org/data/"
    const val METRIC_UNIT: String = "metric"
    const val PREFERENCE_NAME = "WeatherAppPreference"
    const val WEATHER_RESPONSE_DATA= "weather_response_data"
    var Location =  ""

    fun networkAvailability(context: Context) : Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){//Q for API version 29
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true // mode of connection to internet. via wifi
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true // via DATA
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true // Via Ethernet
                else -> false
            }

        }else{
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
}