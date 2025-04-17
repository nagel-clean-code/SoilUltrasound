package ru.sem.soilultrasound.data.settings.sharedprefs

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.sem.soilultrasound.data.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SharedprefSettingsRepository @Inject constructor(
    @ApplicationContext val context: Context
) : SettingsRepository {

    private val sharedPreferences =
        context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)

    override fun saveScannerIp(ip: String) {
        sharedPreferences.edit { putString(SCANNER_IP, ip) }
    }

    override fun getScannerIp() = sharedPreferences.getString(SCANNER_IP, null)

    companion object {
        private const val SCANNER_IP = "SCANNER_IP"
        private const val SETTINGS = "SETTINGS"
    }
}