package ru.sem.soilultrasound.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjoe64.graphview.series.DataPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sem.soilultrasound.data.scanner.ScannerRepository
import ru.sem.soilultrasound.data.settings.SettingsRepository
import ru.sem.soilultrasound.utils.Event
import javax.inject.Inject

@HiltViewModel
class ScannerVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scannerRepository: ScannerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    private val _date = scannerRepository.getDateFlow()
    val date = _date.asStateFlow()

    private var count = 4
    private var currentStartGeneratedTime: Long = 0

    init {
        viewModelScope.launch() {
            date.collect() {
                it.peekContentIfNotHandled()?.let { data ->
                    handleMessageServer(data)
                }
            }
        }
    }

    private fun handleMessageServer(msg: String) {
        val wordList = msg.split('\n', ',', ':')
        when (wordList[0]) {
            RESULT_SCANNING -> {
                _state.value = _state.value.copy(showResultScanning = getPoints(wordList))
            }

            START_GENERATED_TIME -> {
                currentStartGeneratedTime = wordList[1].toLong()
            }
        }
    }

    private fun getPoints(data: List<String>): List<DataPoint> {
        val mutableData = data.toMutableList()
        mutableData.removeAt(0)
        mutableData.remove("")
        var ix = 0
        val listPoint = mutableListOf<DataPoint>()
        repeat(mutableData.size / 2) {
            //TODO Нужно высчитать положение по оси Y
            listPoint.add(DataPoint(0.4, getPosition(mutableData[ix++].toLong())))
        }
        return listPoint
    }

    private fun getPosition(time: Long): Double{
        val c = 331.46 //TODO Скорость звука в среде
        val t = time - currentStartGeneratedTime //Время пути
        return c * t / 2
    }


    fun startScanning() {
        val ip = settingsRepository.getScannerIp()
        if (ip.isNullOrBlank()) {
            _state.value = _state.value.copy(showError = Event(Any()))
            return
        }
        viewModelScope.launch {
            scannerRepository.startWebSocket("ws://$ip/ws")
        }
    }

    private var jobFrequency: Job? = null
    private var jobDutyCycle: Job? = null
    fun setFrequency(value: Int) {
        jobFrequency?.cancel()
        jobFrequency = viewModelScope.launch() {
            scannerRepository.sendFrequency(value)
        }
    }

    fun setDutyCycle(value: Int) {
        jobDutyCycle?.cancel()
        jobDutyCycle = viewModelScope.launch() {
            scannerRepository.sendDutyCycle(value)
        }
    }

    fun setupSignalsCount(count: Int) {
        this.count = count
    }

    fun sendSignals() {
        viewModelScope.launch() {
            scannerRepository.sendSignals(count * SIGNAL_MCS)
        }
    }

    companion object {
        const val SIGNAL_MCS = 25
        const val RESULT_SCANNING = "result_scanning"
        const val START_GENERATED_TIME = "start_generated_time"
    }
}