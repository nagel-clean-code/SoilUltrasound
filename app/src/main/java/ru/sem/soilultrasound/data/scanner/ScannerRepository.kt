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
        if (scannerWebsocket != null) return
        scannerWebsocket = ScannerWebsocket(url, httpClient)
        messageProcessing()
    }

    private suspend fun messageProcessing() {
        scannerWebsocket?.observeMessages()?.collect() {
            data.emit(Event(it))
        }
    }

    fun getDateFlow() = data
    fun sendSignals() = scannerWebsocket?.sendMessage("generateSignals")
    fun stopScanning() =  scannerWebsocket?.sendMessage("stopScanning")
}