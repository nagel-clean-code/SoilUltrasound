package ru.sem.soilultrasound.presentation.scanner

import androidx.compose.ui.graphics.ImageBitmap
import ru.sem.soilultrasound.utils.Event

data class ScannerState(
    val showError: Event<Any>? = null,
    val pointBitmap: ImageBitmap? = null
)
