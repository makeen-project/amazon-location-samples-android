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
import com.amazon.androidquickstartapp.utils.DELAY_5000
import com.amazon.androidquickstartapp.utils.LABEL
import com.amazon.androidquickstartapp.utils.OUTPUT_MESSAGE
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityReverseGeocodeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun reverseGeocodeTest() {
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
        composeTestRule.onNodeWithTag(LABEL)
            .assertIsDisplayed()
        val label = uiDevice.findObject(By.descContains(LABEL))
        assertTrue(
            OUTPUT_MESSAGE,
            !label.text.isNullOrEmpty()
        )
    }
}
