package com.amazon.androidquickstartapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.amazon.androidquickstartapp.MainActivity
import com.amazon.androidquickstartapp.utils.DELAY_10000
import com.amazon.androidquickstartapp.utils.DELAY_5000
import com.amazon.androidquickstartapp.utils.MAP_VIEW
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityMapLoadTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mapLoadTest() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(MAP_VIEW).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        Thread.sleep(DELAY_5000)
        composeTestRule.onNodeWithTag(MAP_VIEW)
            .assertIsDisplayed()
    }
}
