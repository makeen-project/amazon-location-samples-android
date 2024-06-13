package com.amazon.androidquickstartapp.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice



fun grantPermission(permission: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val uiDevice = UiDevice.getInstance(instrumentation)

    uiDevice.executeShellCommand("pm grant ${instrumentation.targetContext.packageName} $permission")
}