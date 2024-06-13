package com.amazon.androidquickstartapp.utils

import android.content.Context
import android.widget.Toast

class Helper {
    fun showToast(message: String, context: Context) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT,
        ).show()
    }
}
