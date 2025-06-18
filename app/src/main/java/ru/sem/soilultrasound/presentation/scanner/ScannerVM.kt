package ru.sem.soilultrasound.presentation.scanner

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val messages: SharedFlow<Event<String>> = _date.asSharedFlow()

    private var count = 4

    init {
        viewModelScope.launch(Dispatchers.Default) {
            messages.collect() {
                it.peekContentIfNotHandled()?.let { data ->
                    handleMessageServer(data)
                }
            }
        }
    }

    private fun handleMessageServer(msg: String) {
        Log.d("Treed handleMessageServer:", Thread.currentThread().name) //TODO проверить
        val wordList = msg.split('\n', ',', ':')
        when (wordList[0]) {
            RESULT_SCANNING -> {
                val currentStartGeneratedTime = wordList[1].toLong()
                val listPoints = getPoints(wordList, currentStartGeneratedTime)
                _state.value = _state.value.copy(showResultScanning = listPoints)
            }
        }
    }

    private fun getPoints(data: List<String>, currentStartGeneratedTime: Long): List<Offset> {
        val mutableData = data.toMutableList()
        mutableData.removeAt(0)
        mutableData.remove("")
        var ix = 0
        val listPoint = mutableListOf<Offset>()
        repeat(20) {
            ++ix
            val position = getPosition(mutableData[ix++].toLong(), currentStartGeneratedTime)
            listPoint.add(Offset(0.4f, position))
        }
        return listPoint
    }

    private fun getPosition(time: Long, currentStartGeneratedTime: Long): Float {
        val c = 0.0343f //TODO Скорость звука в среде
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
    }
}