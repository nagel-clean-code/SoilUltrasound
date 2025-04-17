package ru.sem.soilultrasound.utils

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


fun TextView.showWarning() {
    this.isVisible = true
    postDelayed({
        this.isVisible = false
    }, 3000)
}

inline fun <T> StateFlow<T>.collectStarted(
    lifecycleOwner: LifecycleOwner,
    crossinline block: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            this@collectStarted.collect {
                block(it)
            }
        }
    }
}