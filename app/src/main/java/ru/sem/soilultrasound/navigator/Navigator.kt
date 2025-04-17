package ru.sem.soilultrasound.navigator

import androidx.fragment.app.Fragment

fun Fragment.navigator(): Navigator? {
    return requireActivity() as? Navigator
}

interface Navigator {
    fun showSettingsFragment()
    fun showScannerFragment()
    fun clearBackStack()
    fun goBack()
}