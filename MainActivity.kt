package com.example.mysensor

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.mysensor.SensorData
import com.example.mysensor.SensorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var db: SensorDatabase
    private var x by mutableStateOf(0f)
    private var y by mutableStateOf(0f)
    private var z by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, 100_00)

        db = Room.databaseBuilder(
            applicationContext,
            SensorDatabase::class.java, "sensor_database"
        ).build()

        setContent {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("X Angle: $x", style = MaterialTheme.typography.h6)
                Text("Y Angle: $y", style = MaterialTheme.typography.h6)
                Text("Z Angle: $z", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
                }) {
                    Text("Show Chart")
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0]
            y = event.values[1]
            z = event.values[2]

            lifecycleScope.launch {
                delay(2000) // Delay for 100 ms
                saveData(System.currentTimeMillis(), x, y, z)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Can be implemented as needed
    }

    fun saveData(timestamp: Long, x: Float, y: Float, z: Float) {
        val sensorData = SensorData(timestamp = timestamp, xAngle = x, yAngle = y, zAngle = z)
        lifecycleScope.launch(Dispatchers.IO) {
            db.sensorDao().insert(sensorData)
        }
    }
}
