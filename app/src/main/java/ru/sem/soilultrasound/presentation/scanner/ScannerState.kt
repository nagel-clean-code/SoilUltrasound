package ru.sem.soilultrasound.presentation.scanner

import androidx.compose.ui.geometry.Offset
import ru.sem.soilultrasound.utils.Event

data class ScannerState(
    val showError: Event<Any>? = null,
    val showResultScanning: List<Offset> = emptyList()
)
