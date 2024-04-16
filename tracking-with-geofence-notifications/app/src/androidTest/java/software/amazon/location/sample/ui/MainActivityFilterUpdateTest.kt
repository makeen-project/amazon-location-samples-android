package software.amazon.location.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import software.amazon.location.sample.util.DELAY_10000
import software.amazon.location.sample.util.DELAY_3000
import software.amazon.location.sample.MainActivity
import software.amazon.location.sample.util.DELAY_1000
import software.amazon.location.sample.util.DISTANCE_FILTER_VALUE
import software.amazon.location.sample.util.ICON_DISTANCE_MINUS
import software.amazon.location.sample.util.ICON_DISTANCE_PLUS
import software.amazon.location.sample.util.ICON_TIME_MINUS
import software.amazon.location.sample.util.ICON_TIME_PLUS
import software.amazon.location.sample.util.SIGN_IN
import software.amazon.location.sample.util.SIGN_OUT
import software.amazon.location.sample.util.SWITCH_DISTANCE_FILTER
import software.amazon.location.sample.util.SWITCH_TIME_FILTER
import software.amazon.location.sample.util.TIME_FILTER_VALUE
import software.amazon.location.sample.util.signIn

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityFilterUpdateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setUp() {
        val btnSignIn = uiDevice.findObject(UiSelector().text(SIGN_IN))
        if (btnSignIn.exists()) {
            signIn(composeTestRule)
        }
    }

    @Test
    fun timeFilterValuePlus() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        val iconTimePlus = uiDevice.findObject(UiSelector().descriptionContains(ICON_TIME_PLUS))
        if (iconTimePlus.exists() && iconTimePlus.isEnabled()) {
            iconTimePlus.click()
        }
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(TIME_FILTER_VALUE)
                    .assertTextEquals("Filter value: 31 second")
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
    }

    @Test
    fun distanceFilterValuePlus() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        val iconDistancePlus =
            uiDevice.findObject(UiSelector().descriptionContains(ICON_DISTANCE_PLUS))
        if (iconDistancePlus.exists() && iconDistancePlus.isEnabled()) {
            iconDistancePlus.click()
        }
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(DISTANCE_FILTER_VALUE)
                    .assertTextEquals("Filter value: 31.0 meter")
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
    }


    @Test
    fun timeFilterValueMinus() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        val iconTimePlus = uiDevice.findObject(UiSelector().descriptionContains(ICON_TIME_MINUS))
        if (iconTimePlus.exists() && iconTimePlus.isEnabled()) {
            iconTimePlus.click()
        }
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(TIME_FILTER_VALUE)
                    .assertTextEquals("Filter value: 29 second")
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
    }

    @Test
    fun distanceFilterValueMinus() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        val iconDistancePlus =
            uiDevice.findObject(UiSelector().descriptionContains(ICON_DISTANCE_MINUS))
        if (iconDistancePlus.exists() && iconDistancePlus.isEnabled()) {
            iconDistancePlus.click()
        }
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(DISTANCE_FILTER_VALUE)
                    .assertTextEquals("Filter value: 29.0 meter")
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
    }

    @After
    fun tearDown() {
        Thread.sleep(DELAY_1000)
        val btnSignOut = uiDevice.findObject(UiSelector().text(SIGN_OUT))
        if (btnSignOut.exists()) {
            composeTestRule.onNodeWithText(SIGN_OUT).performClick()
            Thread.sleep(DELAY_3000)
            composeTestRule.waitForIdle()
        }
    }
}
