package com.example.mysensor
import androidx.room.*

@Entity
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val xAngle: Float,
    val yAngle: Float,
    val zAngle: Float
)
