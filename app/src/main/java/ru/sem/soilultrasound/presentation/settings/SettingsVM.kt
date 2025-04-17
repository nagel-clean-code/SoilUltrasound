package ru.sem.soilultrasound.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.sem.soilultrasound.data.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    fun getIpAddress() = settingsRepository.getScannerIp()
    fun saveIpAddress(ip: String) = settingsRepository.saveScannerIp(ip)
}