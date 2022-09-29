package com.example.myweatherapplication.currentForecastModels

import java.io.Serializable

data class Wind (
    val speed : Double,
    val deg : Int
) : Serializable