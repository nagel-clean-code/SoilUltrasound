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

    init {
        viewModelScope.launch() {
            date.collect() {
                it.peekContentIfNotHandled()?.let { data ->
                    val wordList = data.split('\n', ',')
                    if (wordList[0] == RESULT_SCANNING) {
                        _state.value = _state.value.copy(showResultScanning = getPoints(wordList))
                    }
                }
            }
        }
    }

    private fun getPoints(data: List<String>): List<DataPoint> {
        val mutableData = data.toMutableList()
        mutableData.removeAt(0)
        mutableData.remove("")
        val dataInt = mutableData.map { it.toInt().toDouble() }
        var ix = 0
        val listPoint = mutableListOf<DataPoint>()
        repeat(dataInt.size / 2) {
            listPoint.add(DataPoint(dataInt[ix++], dataInt[ix++]))
        }
        return listPoint
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
    }
}