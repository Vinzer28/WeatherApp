package com.example.myweatherapplication.currentForecastModels

import java.io.Serializable

data class Sys (
    val type : Int,
    val message : Double,
    val country : String,
    val sunrise : Int,
    val sunset : Int
) : Serializable