package com.example.myweatherapplication.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.myweatherapplication.Constants
import com.example.myweatherapplication.R
import com.example.myweatherapplication.databinding.ActivityFiveDaysForecastBinding
import com.example.myweatherapplication.futureForecastModel.FutureWeather
import com.example.myweatherapplication.network.WeatherService
import com.google.android.gms.location.*
import com.google.gson.Gson
import retrofit.*
import java.util.*
import java.util.concurrent.Future

class fiveDaysForecast : AppCompatActivity() {
    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var mSharedPreferences: SharedPreferences
    private var mProgressDialog : Dialog? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private lateinit var binding: ActivityFiveDaysForecastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_five_days_forecast)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
        requestLocationData()
    }

    private fun getFutureWeatherDetails() {
        if (Constants.networkAvailability(this@fiveDaysForecast)) {
            val retrofit: Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
            val service : WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<FutureWeather> = service.getFutureWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT, Constants.Location, Constants.APP_ID
            )

            showCustomProgressDialog()

            listCall.enqueue(object : Callback<FutureWeather> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: Response<FutureWeather>, retrofit: Retrofit?) {
                   if (response.isSuccess) {
                       hideProgressDialog()
                       val weatherList : FutureWeather = response.body()
                       Log.i("Response Result", "$weatherList")
                       val weatherResponseJsonString = Gson().toJson(weatherList)
                       val editor = mSharedPreferences.edit()
                       editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                       editor.apply()
                       setupUI()
                   }else{
                       val rc = response.code()
                       hideProgressDialog()
                       when(rc){
                           400 -> {
                               Log.e("Error 400", "Bad Connection")
                           }
                           404 -> {
                               Log.e("Error 404", "Not Found")
                           }
                           else -> {
                               Log.e("Error", "Error")
                           }
                       }
                   }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("Error!", t!!.message.toString())
                    hideProgressDialog()
                }
            })
        } else {
            Toast.makeText(this@fiveDaysForecast, "Please connect to the internet.",
                Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun setupUI(){
        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA,"")

        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList =
                Gson().fromJson(weatherResponseJsonString, com.example.myweatherapplication.
                futureForecastModel.FutureWeather::class.java)
            for (i in weatherList.list.indices) {
                binding.textLocation.text = weatherList.city.name
                binding.tempText1.text = weatherList.list[0].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                binding.tempText2.text = weatherList.list[8].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                binding.tempText3.text = weatherList.list[16].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                binding.tempText4.text = weatherList.list[24].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                binding.tempText5.text = weatherList.list[32].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())

                binding.tempDate1.text = weatherList.list[0].dt_txt
                binding.tempDate2.text = weatherList.list[8].dt_txt
                binding.tempDate3.text = weatherList.list[16].dt_txt
                binding.tempDate4.text = weatherList.list[24].dt_txt
                binding.tempDate5.text = weatherList.list[32].dt_txt

                binding.status1.text = weatherList.list[0].weather[0].description
                binding.status2.text = weatherList.list[8].weather[0].description
                binding.status3.text = weatherList.list[16].weather[0].description
                binding.status4.text = weatherList.list[24].weather[0].description
                binding.status5.text = weatherList.list[32].weather[0].description

                when (weatherList.list[0].weather[0].icon) {
                    "01d" -> binding.TempImage1.setImageResource(R.drawable.sunny)
                    "02d" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "03d" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "04d" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "04n" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "10d" -> binding.TempImage1.setImageResource(R.drawable.rain)
                    "11d" -> binding.TempImage1.setImageResource(R.drawable.storm)
                    "13d" -> binding.TempImage1.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "02n" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "03n" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "10n" -> binding.TempImage1.setImageResource(R.drawable.cloud)
                    "11n" -> binding.TempImage1.setImageResource(R.drawable.rain)
                    "13n" -> binding.TempImage1.setImageResource(R.drawable.snowflake)
                }

                when (weatherList.list[8].weather[0].icon) {
                    "01d" -> binding.TempImage2.setImageResource(R.drawable.sunny)
                    "02d" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "03d" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "04d" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "04n" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "10d" -> binding.TempImage2.setImageResource(R.drawable.rain)
                    "11d" -> binding.TempImage2.setImageResource(R.drawable.storm)
                    "13d" -> binding.TempImage2.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "02n" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "03n" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "10n" -> binding.TempImage2.setImageResource(R.drawable.cloud)
                    "11n" -> binding.TempImage2.setImageResource(R.drawable.rain)
                    "13n" -> binding.TempImage2.setImageResource(R.drawable.snowflake)
                }

                when (weatherList.list[16].weather[0].icon) {
                    "01d" -> binding.TempImage3.setImageResource(R.drawable.sunny)
                    "02d" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "03d" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "04d" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "04n" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "10d" -> binding.TempImage3.setImageResource(R.drawable.rain)
                    "11d" -> binding.TempImage3.setImageResource(R.drawable.storm)
                    "13d" -> binding.TempImage3.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "02n" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "03n" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "10n" -> binding.TempImage3.setImageResource(R.drawable.cloud)
                    "11n" -> binding.TempImage3.setImageResource(R.drawable.rain)
                    "13n" -> binding.TempImage3.setImageResource(R.drawable.snowflake)
                }

                when (weatherList.list[24].weather[0].icon) {
                    "01d" -> binding.TempImage4.setImageResource(R.drawable.sunny)
                    "02d" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "03d" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "04d" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "04n" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "10d" -> binding.TempImage4.setImageResource(R.drawable.rain)
                    "11d" -> binding.TempImage4.setImageResource(R.drawable.storm)
                    "13d" -> binding.TempImage4.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "02n" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "03n" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "10n" -> binding.TempImage4.setImageResource(R.drawable.cloud)
                    "11n" -> binding.TempImage4.setImageResource(R.drawable.rain)
                    "13n" -> binding.TempImage4.setImageResource(R.drawable.snowflake)
                }

                when (weatherList.list[32].weather[0].icon) {
                    "01d" -> binding.TempImage5.setImageResource(R.drawable.sunny)
                    "02d" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "03d" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "04d" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "04n" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "10d" -> binding.TempImage5.setImageResource(R.drawable.rain)
                    "11d" -> binding.TempImage5.setImageResource(R.drawable.storm)
                    "13d" -> binding.TempImage5.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "02n" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "03n" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "10n" -> binding.TempImage5.setImageResource(R.drawable.cloud)
                    "11n" -> binding.TempImage5.setImageResource(R.drawable.rain)
                    "13n" -> binding.TempImage5.setImageResource(R.drawable.snowflake)
                }
            }
        }
    }

    private fun getUnit(value :String): String? {
        var value = "°C"
        if("US" == value  || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm", Locale.CHINA)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            getFutureWeatherDetails()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


}