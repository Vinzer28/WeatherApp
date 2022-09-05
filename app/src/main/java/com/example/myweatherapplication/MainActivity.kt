package com.example.myweatherapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.myweatherapplication.databinding.ActivityMainBinding
import com.example.myweatherapplication.models.WeatherResponse
import com.example.myweatherapplication.network.WeatherService
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.util.*

class MainActivity : AppCompatActivity() {
//    Data Binding
    private lateinit var binding : ActivityMainBinding

    private lateinit var mFusedLocationClient: FusedLocationProviderClient //required in getting the location (lat, lon)

    private var mProgressDialog : Dialog? = null


    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

//    For data storage
    private lateinit var mSharedPreferences : SharedPreferences
// To bo used in Dark Mode Feature
    internal  lateinit var nightSwitch : Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)

//        initialize mSharedPreferences
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME,
            Context.MODE_PRIVATE) //makes the data available for this app only
        setupUI()

        if(!enableLocation()){
            Toast.makeText(
                this, "Your location is turned OFF. Please enable.", Toast.LENGTH_SHORT
            ).show()
//            Redirects to Location Settings
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else {
            Dexter.withActivity(this).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()){
                            getLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied){
                            Toast.makeText(this@MainActivity,
                                "You have denied permissions access, need to grant.",
                                Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
        }

//      Dark Mode Feature
        nightSwitch = binding.darkMode as Switch
        nightSwitch.setOnClickListener {
            if (nightSwitch.isChecked){
                binding.bg.setBackgroundResource(R.drawable.dark_theme)
                binding.darkMode.setTextColor(Color.WHITE)
                Toast.makeText(this, "Night mode enabled.", Toast.LENGTH_SHORT).show()
            }else{
                binding.bg.setBackgroundResource(R.drawable.light_theme)
                binding.darkMode.setTextColor(Color.BLACK)
                Toast.makeText(this, "Night mode disabled", Toast.LENGTH_SHORT).show()
            }
        }

//        Search for other locations
        binding.searchButton.setOnClickListener {
            var otherLocation : String = binding.otherLocation.text.toString()
            Constants.Location = otherLocation
            if (otherLocation == ""){
                getLocationWeatherDetails()
            }else{
                getCurrentLocation()
            }
        }
    }
//         Device's Location Settings
    private fun enableLocation() :Boolean{
        //This provides access to the system location services.
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)//GPS enabled
    }

//When App request for permission is denied / Action needed prompt
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("Permissions disabled. Enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    @SuppressLint("MissingPermission") //Suppresses missing permission
    private fun getLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLatitude = mLastLocation!!.latitude
            Log.i("Current Latitude", "$mLatitude")

            mLongitude = mLastLocation.longitude
            Log.i("Current Longitude", "$mLongitude")
            getLocationWeatherDetails()

        }
    }

    private fun  getLocationWeatherDetails(){
        if(Constants.networkAvailability(this@MainActivity)){
            val retrofit : Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(
                GsonConverterFactory.create()).build()

            val service : WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall : Call<WeatherResponse> = service.getWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT, Constants.APP_ID
            )
            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse>{
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: Response<WeatherResponse>?, retrofit: Retrofit?) {
                    if(response!!.isSuccess){

                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                        editor.apply()
                        setupUI()

                        Log.i("Response Result", "$weatherList")
                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }else -> {
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
        }else{
            Toast.makeText(this@MainActivity,
                "Please connect to the internet.", Toast.LENGTH_SHORT).show()
        }

    }
// Progress Bar
    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }

    private fun setupUI(){

        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (i in weatherList.weather.indices){
                Log.i("Weather Name", weatherList.weather.toString())
                binding.tvMain.text = weatherList.weather[i].main
                binding.tvMainDescription.text = weatherList.weather[i].description
                binding.tvTemp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString()) //unit used from the user's location

                binding.tvSunriseTime.text = timeStamp(weatherList.sys.sunrise.toLong()) + " am"
                binding.tvSunsetTime.text = timeStamp(weatherList.sys.sunset.toLong()) + " pm"

                binding.tvHumidity.text = weatherList.main.humidity.toString() + " %"
                binding.tvMin.text = weatherList.main.temp_min.toString() + " min"
                binding.tvMax.text = weatherList.main.temp_max.toString() + " max"
                binding.tvSpeed.text = weatherList.wind.speed.toString()
                binding.tvName.text = weatherList.name
                binding.tvCountry.text = weatherList.sys.country

                when (weatherList.weather[i].icon) {
                    "01d" -> binding.ivMain.setImageResource(R.drawable.sunny)
                    "02d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "03d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "04d" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "04n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "10d" -> binding.ivMain.setImageResource(R.drawable.rain)
                    "11d" -> binding.ivMain.setImageResource(R.drawable.storm)
                    "13d" -> binding.ivMain.setImageResource(R.drawable.snowflake)
                    "01n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "02n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "03n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "10n" -> binding.ivMain.setImageResource(R.drawable.cloud)
                    "11n" -> binding.ivMain.setImageResource(R.drawable.rain)
                    "13n" -> binding.ivMain.setImageResource(R.drawable.snowflake)

                }
            }
        }

    }
    private fun getUnit(value : String):String?{
        var value = "°C"
        if("US" == value  || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }

//    Time Format for Sunrise and Sunset Time
    private fun timeStamp(timex : Long) : String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat")
        val sdf = SimpleDateFormat("HH:mm", Locale.CHINA) //gmt + 8
        sdf.timeZone = TimeZone.getDefault()
        return  sdf.format(date)
    }

//      Refresh function
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
//      Makes the Refresh button functional
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_refresh -> {
                getLocationWeatherDetails()
                true
            }else -> super.onOptionsItemSelected(item)
        }
    }


    private fun  getCurrentLocation(){
        if(Constants.networkAvailability(this@MainActivity)){
            val retrofit : Retrofit = Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(
                GsonConverterFactory.create()).build()

            val service : WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall : Call<WeatherResponse> = service.getLocationWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT, Constants.Location, Constants.APP_ID
            )
            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse>{
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: Response<WeatherResponse>?, retrofit: Retrofit?) {
                    if(response!!.isSuccess){

                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                        editor.apply()
                        setupUI()

                        Log.i("Response Result", "$weatherList")
                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }else -> {
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
        }else{
            Toast.makeText(this@MainActivity,
                "Please connect to the internet.", Toast.LENGTH_SHORT).show()
        }

    }

}