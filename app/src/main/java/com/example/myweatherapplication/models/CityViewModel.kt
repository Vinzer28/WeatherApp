package com.example.myweatherapplication.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myweatherapplication.db.CityRepository
import com.example.myweatherapplication.db.ManageCities
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CityViewModel(private val repository: CityRepository) : ViewModel() {


    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete: ManageCities

    val inputName = MutableLiveData<String?>()
    val saveOrUpdateButtonText = MutableLiveData<String>()
    val clearAllOrDeleteButtonText = MutableLiveData<String>()
//    var cities = MutableLiveData<MutableList<String>>()
    val cities = repository.getAllCities()

    init {
//        cities = repository.getAllCities()
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun saveOrUpdate(){
        if (isUpdateOrDelete) {
            //subscriberToUpdateOrDelete.CityName = inputName.value!!
            update(subscriberToUpdateOrDelete)

        } else {
            val name = inputName.value!!
            insert(ManageCities(0,name))
            inputName.value = null

        }
    }


//    fun saveOrUpdate() = runBlocking {
//        if (isUpdateOrDelete) {
//            //subscriberToUpdateOrDelete.CityName = inputName.value!!
//            update(subscriberToUpdateOrDelete)
//
//        } else {
//            val name = inputName.value!!
//            insert2(ManageCities(cityName = name))
//            inputName.value = null
//
//        }
//    }

//    fun getCities() = runBlocking {
//        repository.getCities().onEach {
//            cities = it.toMutableList()
//        }.launchIn(viewModelScope)
//    }


    fun clearAllOrDelete() {
        if (isUpdateOrDelete) {
            delete(subscriberToUpdateOrDelete)
        } else {
            clearAll()
        }
    }

    fun insert(manageCities: ManageCities) = runBlocking {
        repository.insert(manageCities)

    }

//    fun insert2(manageCities: ManageCities) = runBlocking {
//        repository.insert2(manageCities).launchIn(viewModelScope)
//
//    }

    fun update(manageCities: ManageCities) = viewModelScope.launch {
        //Constants.LOCATION = inputName.value!!
        inputName.value = null
        isUpdateOrDelete = false
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun delete(manageCities: ManageCities) = viewModelScope.launch {
        repository.delete(manageCities)
        inputName.value = null
        isUpdateOrDelete = false
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"

    }

    private fun clearAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun initUpdateAndDelete(manageCities: ManageCities) {
        inputName.value = manageCities.cityName
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = manageCities
        saveOrUpdateButtonText.value = "Cancel"
        clearAllOrDeleteButtonText.value = "Delete"

    }

}