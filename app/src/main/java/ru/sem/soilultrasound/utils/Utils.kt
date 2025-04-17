package ru.sem.soilultrasound.utils

import android.content.Context
import android.widget.Toast

fun showToast(resId: Int, context: Context?) {
    if (context != null) Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
}

fun showToast(text: String?, context: Context?) {
    if (context != null) Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
