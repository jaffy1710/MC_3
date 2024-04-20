package com.example.mysensor
import androidx.room.*

@Dao
interface SensorDao {
    @Insert
    suspend fun insert(sensorData: SensorData)

    @Query("SELECT * FROM sensordata")
    suspend fun getAll(): List<SensorData>
}