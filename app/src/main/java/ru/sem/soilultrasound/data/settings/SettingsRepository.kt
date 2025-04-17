package ru.sem.soilultrasound.data.settings


interface SettingsRepository {
    fun saveScannerIp(ip: String)
    fun getScannerIp(): String?
}