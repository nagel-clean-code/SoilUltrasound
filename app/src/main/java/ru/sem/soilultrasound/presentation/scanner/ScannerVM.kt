package ru.sem.soilultrasound.presentation.scanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class ScannerVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scannerRepository: ScannerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

    private val _messages = scannerRepository.getDateFlow()
    val messages: SharedFlow<Event<String>> = _messages.asSharedFlow()

    private var wordList: List<String> = emptyList()

    private var bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
    private var canvas = Canvas(bitmap)
    private val paint = Paint().apply {
        color = Color.Black.toArgb()
        style = Paint.Style.FILL
    }

    init {
        connectionWs()
        viewModelScope.launch(Dispatchers.Default) {
            messages.collect() {
                it.peekContentIfNotHandled()?.let { data ->
                    handleMessageServer(data)
                }
            }
        }
        viewModelScope.launch {
            wordList = listOf("100", "200", "300", "600", "900", "1200")
            repeat(100) {
                _state.value = _state.value.copy(pointBitmap = createBitmap())
                delay(1000)
            }
        }
    }

    private fun handleMessageServer(msg: String) {
        Log.d("Treed handleMessageServer:", Thread.currentThread().name) //TODO проверить
        wordList = msg.split('\n', ',', ':')
        when (wordList[0]) {
            RESULT_SCANNING -> {
                _state.value = _state.value.copy(pointBitmap = createBitmap())
            }
        }
    }

    private fun createBitmap(): ImageBitmap {
        if (wordList.isEmpty()) return bitmap.asImageBitmap()
        val currentStartGeneratedTime = wordList[1].toLong()
        val mutableData = wordList.toMutableList()
        mutableData.removeAt(0)
        mutableData.remove("")
        mutableData.removeAt(0)//TODO проверить работает ли
        var ix = 0
        while (ix < mutableData.size) {
            val sound = mutableData[ix++].toInt() //TODO нуюно будет для определения цвета
            val time = mutableData[ix++].toLong()
            val alpha = mutableData[ix++].toFloat()
            val position = getPosition(time, currentStartGeneratedTime)
            val x = rotationCoordinateX(position, alpha)
            val y = rotationCoordinateY(position, alpha)
            canvas.drawPoint(x, y, paint)
        }
        return bitmap.asImageBitmap()
    }

    private fun getPosition(time: Long, currentStartGeneratedTime: Long): Float {
        val c = 0.0343f //TODO Скорость звука в среде
        val t = time - currentStartGeneratedTime //Время пути
        return c * t / 2
    }

    private fun rotationCoordinateX(y: Float, alpha: Float) = CENTER_X - (y * sin(alpha))
    private fun rotationCoordinateY(y: Float, alpha: Float) = (y * cos(alpha))

    fun clear() {
        scannerRepository.stopScanning()
        bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        wordList = emptyList()
        _state.value = _state.value.copy(pointBitmap = bitmap.asImageBitmap())
    }

    private fun connectionWs() {
        val ip = settingsRepository.getScannerIp()
        if (ip.isNullOrBlank()) {
            _state.value = _state.value.copy(showError = Event(Any()))
            return
        }
        viewModelScope.launch {
            scannerRepository.startWebSocket("ws://$ip/ws")
        }
    }

    fun sendSignals() {
        viewModelScope.launch() {
            scannerRepository.sendSignals()
        }
    }

    companion object {
        private const val CENTER_X = 500f
        private const val BITMAP_WIDTH = 1000
        private const val BITMAP_HEIGHT = 1700
        const val RESULT_SCANNING = "result_scanning"
    }
}