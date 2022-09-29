package com.example.myweatherapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDAO {
    @Insert
    fun insertCity(manageCities: ManageCities) : Long
    @Delete
    fun deleteCity(manageCities: ManageCities)
    @Query ("DELETE FROM City_data_table")
    fun deleteAll()
    @Query("SELECT * FROM City_data_table")
    fun getAllCity() : LiveData<List<ManageCities>>
}