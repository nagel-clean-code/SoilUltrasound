package ru.sem.soilultrasound.data.scanner

import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import ru.sem.soilultrasound.utils.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerRepository @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private var scannerWebsocket: ScannerWebsocket? = null
    private val data = MutableSharedFlow<Event<String>>(extraBufferCapacity = 1024)

    suspend fun startWebSocket(url: String) {
        scannerWebsocket = ScannerWebsocket(url, httpClient)
        messageProcessing()
    }

    private suspend fun messageProcessing() {
        scannerWebsocket?.observeMessages()?.collect() {
            data.emit(Event(it))
        }
    }

    fun getDateFlow() = data

    fun sendDutyCycle(value: Int) {
        scannerWebsocket?.sendMessage("setDutyCycle $value")
    }

    fun sendSignals(value: Int) {
        scannerWebsocket?.sendMessage("generateSignals $value")
    }

    fun sendFrequency(value: Int) {
        scannerWebsocket?.sendMessage("setFreq $value")
    }
}