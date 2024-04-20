package com.example.mysensor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(appDatabase: SensorDatabase) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    //    init {
//        viewModelScope.launch {
//            val sensorData = appDatabase.sensorDao().getAll()
//            _state.value = HistoryState(
//                xEntries = sensorData.mapIndexed { index, data -> Entry(index.toFloat(), data.xAngle) },
//                yEntries = sensorData.mapIndexed { index, data -> Entry(index.toFloat(), data.yAngle) },
//                zEntries = sensorData.mapIndexed { index, data -> Entry(index.toFloat(), data.zAngle) }
//            )
//        }
//    }
    private val _chartData = MutableStateFlow(Triple(emptyList<Entry>(), emptyList<Entry>(), emptyList<Entry>()))
    val chartData: StateFlow<Triple<List<Entry>, List<Entry>, List<Entry>>> = _chartData

    init {
        viewModelScope.launch {
            val dataList = appDatabase.sensorDao().getAll() // Assuming this is a suspend function
            _chartData.value = Triple(
                dataList.map { Entry(it.timestamp.toFloat(), it.xAngle) },
                dataList.map { Entry(it.timestamp.toFloat(), it.yAngle) },
                dataList.map { Entry(it.timestamp.toFloat(), it.zAngle) }
            )
        }
    }

    data class HistoryState(
        val xEntries: List<Entry> = emptyList(),
        val yEntries: List<Entry> = emptyList(),
        val zEntries: List<Entry> = emptyList()
    )
}