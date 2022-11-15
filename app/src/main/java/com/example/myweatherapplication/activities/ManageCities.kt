package com.example.myweatherapplication.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myweatherapplication.Constants
import com.example.myweatherapplication.MainActivity
import com.example.myweatherapplication.R
import com.example.myweatherapplication.databinding.ActivityManageCitiesBinding
import com.example.myweatherapplication.db.CityDatabase
import com.example.myweatherapplication.db.CityRepository
import com.example.myweatherapplication.db.CityRepositoryImpl
import com.example.myweatherapplication.db.ManageCities
import com.example.myweatherapplication.models.CityViewModel
import com.example.myweatherapplication.models.CityViewModelFactory
import com.example.myweatherapplication.utilities.CityAdapter

class ManageCities : AppCompatActivity() {
    private lateinit var binding: ActivityManageCitiesBinding
    private lateinit var cityViewModel: CityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_cities)
        val dao = CityDatabase.getInstance(application).cityDAO
        val repository = CityRepositoryImpl(dao)
        val factory = CityViewModelFactory(repository)
        cityViewModel = ViewModelProvider(this, factory)[CityViewModel::class.java]
        binding.myViewModel = cityViewModel
        binding.lifecycleOwner = this
        initRecyclerView()

        binding.viewData.setOnClickListener() {
            Constants.Location = binding.nameText.text.toString()

            val intent = Intent(this@ManageCities, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.cityRecyclerView.layoutManager = LinearLayoutManager(this)
        displayCityList()
    }

    private fun displayCityList() {
        cityViewModel.cities.observe(this, Observer {
            Log.i("MYTAG", it.toString())
            binding.cityRecyclerView.adapter = CityAdapter(it) { selectedItem: ManageCities ->
                listItemClicked(
                    selectedItem
                )
            }
        })
    }

    private fun listItemClicked(manageCities: ManageCities) {
        cityViewModel.initUpdateAndDelete(manageCities)
    }
}