package com.amazon.androidquickstartapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.amazon.androidquickstartapp.MainActivity
import com.amazon.androidquickstartapp.utils.DELAY_10000
import com.amazon.androidquickstartapp.utils.DELAY_5000
import com.amazon.androidquickstartapp.utils.LABEL
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityMapSwipeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mapSwipeTest() {
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
        swipeUp()
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(LABEL).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
    }

    private fun swipeUp() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val displayWidth = uiDevice.displayWidth
        val displayHeight = uiDevice.displayHeight
        val startY = displayHeight / 2
        val endY = startY / 8
        val startX = displayWidth / 2
        uiDevice.swipe(startX, startY, startX, endY, 10)
    }
}
