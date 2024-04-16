package software.amazon.location.sample.helper

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.widget.Toast

class Helper {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        )
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT,
        ).show()
    }
}
