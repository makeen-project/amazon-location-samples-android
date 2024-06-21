package com.amazon.androidquickstartapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.amazon.androidquickstartapp.MainActivity
import com.amazon.androidquickstartapp.utils.DELAY_10000
import com.amazon.androidquickstartapp.utils.DELAY_2000
import com.amazon.androidquickstartapp.utils.DELAY_5000
import com.amazon.androidquickstartapp.utils.LABEL
import com.amazon.androidquickstartapp.utils.MY_LOCATION
import com.amazon.androidquickstartapp.utils.START_TRACKING
import com.amazon.androidquickstartapp.utils.STOP_TRACKING
import com.amazon.androidquickstartapp.utils.grantPermission
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityStartStopTrackingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun startStopTrackingTest() {
        grantPermission("android.permission.ACCESS_COARSE_LOCATION")
        grantPermission("android.permission.ACCESS_FINE_LOCATION")
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(LABEL).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        Thread.sleep(DELAY_5000)
        uiDevice.findObject(By.descContains(MY_LOCATION))?.click()
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(LABEL).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        composeTestRule.waitForIdle()
        uiDevice.findObject(By.descContains(START_TRACKING))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(DELAY_10000)
        uiDevice.findObject(By.descContains(STOP_TRACKING))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(DELAY_2000)
    }
}
