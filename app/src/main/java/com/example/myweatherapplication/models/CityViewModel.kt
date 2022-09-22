package com.example.myweatherapplication.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myweatherapplication.db.CityRepository
import com.example.myweatherapplication.db.ManageCities
import kotlinx.coroutines.launch

class CityViewModel (private val repository: CityRepository) : ViewModel(){


    val cities = repository.cities
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete : ManageCities

    val inputName = MutableLiveData<String?>()
    val saveOrUpdateButtonText = MutableLiveData<String>()
    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    init {
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun saveOrUpdate(){
        if(isUpdateOrDelete){
            //subscriberToUpdateOrDelete.CityName = inputName.value!!
            update(subscriberToUpdateOrDelete)

        }else {
            val name = inputName.value!!
            insert(ManageCities(0, name))
            inputName.value = null
        }
    }



    fun clearAllOrDelete(){
        if(isUpdateOrDelete){
            delete(subscriberToUpdateOrDelete)
        }else{
            clearAll()
        }
    }

    fun insert(manageCities: ManageCities)= viewModelScope.launch {
        repository.insert(manageCities)

    }

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

    private fun clearAll()= viewModelScope.launch {
        repository.deleteAll()
    }
    fun initUpdateAndDelete(manageCities: ManageCities){
        inputName.value = manageCities.CityName
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = manageCities
        saveOrUpdateButtonText.value = "Cancel"
        clearAllOrDeleteButtonText.value = "Delete"

    }

}