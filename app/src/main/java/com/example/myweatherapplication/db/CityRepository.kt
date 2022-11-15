package com.example.myweatherapplication.db

import androidx.lifecycle.LiveData


//class CityRepository(private val dao : CityDAO) {
//
//    val cities = dao.getAllCities()
//
//    fun insert(manageCities: ManageCities){
//        dao.insertCity(manageCities)
//    }
//
//   fun delete(manageCities: ManageCities){
//        dao.deleteCity(manageCities)
//    }
//
//   fun deleteAll(){
//        dao.deleteAll()
//    }
//}
interface CityRepository{
    suspend fun insert(manageCities: ManageCities)
    suspend fun delete(manageCities: ManageCities)
    suspend fun deleteAll()
    fun getAllCities(): LiveData<List<ManageCities>>

}

class CityRepositoryImpl(private val dao : CityDAO): CityRepository {
    val cities = dao.getAllCities()

    override suspend fun insert (manageCities: ManageCities){
        dao.insertCity(manageCities)
    }

    override suspend fun delete(manageCities: ManageCities){
        dao.deleteCity(manageCities)
    }

    override suspend fun deleteAll(){
        dao.deleteAll()
    }

    override fun getAllCities(): LiveData<List<ManageCities>>{
        return dao.getAllCities()
    }
}