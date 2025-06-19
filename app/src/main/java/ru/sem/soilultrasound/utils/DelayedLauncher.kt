package ru.sem.soilultrasound.utils

import java.util.concurrent.atomic.AtomicBoolean

class DelayedLauncher {

    private val isBlockStarted = AtomicBoolean(false)
    private var delayedLaunch: (() -> Unit)? = null

    /**
     * Если запуск уже заблокирован - вернёт false и обновит функцию отложенного запуска
     * Если не заблокирован - то просто блокирует и возвращает true
     */
    fun tryBlock(delayedFunction: () -> Unit): Boolean {
        if (isBlockStarted.get()) {
            delayedLaunch = delayedFunction
            return false
        } else {
            isBlockStarted.set(true)
            return true
        }
    }

    fun start() {
        val startFun = delayedLaunch
        isBlockStarted.set(false)
        startFun?.invoke()
    }
}