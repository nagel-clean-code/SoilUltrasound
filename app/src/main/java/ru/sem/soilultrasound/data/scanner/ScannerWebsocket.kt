package ru.sem.soilultrasound.data.scanner

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ScannerWebsocket(
    private val url: String,
    okHttpClient: OkHttpClient
) {

    private val flow = MutableSharedFlow<String>(5, 5)

    private val webSocket: WebSocket = okHttpClient.newWebSocket(
        Request.Builder().url(url).build(),
        object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                flow.tryEmit(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure \n  Cause - ${t.cause.toString()}")
                flow.tryEmit("\nonFailure: Ошибка соединения WebSocket\n")
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d(TAG, "Opening socket\n response code $response")
                flow.tryEmit("\nonOpen: Успешное соединение\n")
            }
        }
    )

    fun observeMessages(): MutableSharedFlow<String> = flow

    fun sendMessage(message: String) {
        Log.d(TAG, "send message $message")
        webSocket.send(message)
    }

    fun onCleared() {
        Log.d(TAG, "stef: close websocket: url: $url")
        webSocket.close(1000, "")
    }

    companion object {
        const val TAG = "ScannerWebsocket:"
    }
}