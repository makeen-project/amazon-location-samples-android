package com.amazon.androidquickstartapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.amazon.androidquickstartapp.R

class DialogHelper(private val context: Context) {
    fun showSettingsDialog(isBackgroundLocationRequired: Boolean, onOpenAppSettings: () -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(context.getString(R.string.label_permission_required))

        val message = if (isBackgroundLocationRequired) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                context.getString(R.string.error_label_location)
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                "For full functionality, we need your background location permission. " +
                        "Please go to Settings > Apps & notifications > Advanced > App permissions > Location > ${
                            context.getString(R.string.app_name)
                        } and select 'Allow all the time'."
            } else {
                context.getString(R.string.error_label_location)
            }
        } else {
            "To continue, please allow the required permission. " +
                    "You can do this by going to Settings > Apps > ${context.getString(R.string.app_name)} > Permissions, " +
                    "and then enabling the necessary permission."
        }
        dialogBuilder.setMessage(message)

        dialogBuilder.setPositiveButton(context.getString(R.string.label_open_settings)) { dialog, _ ->
            dialog.dismiss()
            if (isBackgroundLocationRequired && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            } else {
                onOpenAppSettings()
            }
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.label_cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
