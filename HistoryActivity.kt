package com.example.mysensor
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.LaunchedEffect

import androidx.room.Room
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appDatabase = Room.databaseBuilder(
                applicationContext,
                SensorDatabase::class.java, "sensor_database"
            ).fallbackToDestructiveMigration().build()

            val viewModel: HistoryViewModel by viewModels { HistoryViewModelFactory(appDatabase) }

            val chartDataState = viewModel.chartData.collectAsState()
            val chartData = chartDataState.value
            val (xData, yData, zData) = chartData
            if (xData.isNotEmpty()) {
                HistoryContent(xData, yData, zData)
            }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun HistoryContent(xData: List<Entry>, yData: List<Entry>, zData: List<Entry>) {

        Scaffold(topBar = {
            TopAppBar(title = { Text("Sensor Data History") })
        }) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Log.d("dataofy", "$yData")
                LineChartComposable("X-Axis", xData)
                LineChartComposable("Y-Axis", yData)
                LineChartComposable("Z-Axis", zData)
            }
        }

        LaunchedEffect(Unit) {
            delay(2000)
        }
    }

    @Composable
    fun LineChartComposable(title: String, entries: List<Entry>) {
        Log.d("LineChartComposable", "Entries: $entries")

        val firstTenEntries = entries.take(10)
        val baseTime = firstTenEntries.minByOrNull { it.x }?.x ?: 0f
        val normalizedEntries = firstTenEntries.map { Entry((it.x - baseTime) / 1000, it.y) }
        Log.d("LineChartComposable3", "Entries: $normalizedEntries")
        val lineDataSet = LineDataSet(normalizedEntries, "Label").apply {
            color = android.graphics.Color.BLUE
            valueTextColor = android.graphics.Color.RED
            lineWidth = 2f
            setDrawValues(false)
            setDrawCircles(false)
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            factory = { context ->
                LineChart(context).apply {
                    data = LineData(lineDataSet)
                    Log.d("LineChartComposable2", "Chart Data: ${data.toString()}")
                    axisLeft.granularity = 1f
                    xAxis.granularity = 1f
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = XAxisValueFormatter(baseTime)
                    description.text = title
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    invalidate()
                }
            }
        )
    }


    class XAxisValueFormatter(private val baseTime: Float) : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        override fun getFormattedValue(value: Float): String {
            val actualTimestamp = baseTime + (value * 1000)  // Adjust back to milliseconds
            return dateFormat.format(Date(actualTimestamp.toLong()))
        }
    }
}

