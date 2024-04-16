package software.amazon.location.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import software.amazon.location.sample.MainActivity
import software.amazon.location.sample.util.BTN_TRACKING_BACKGROUND
import software.amazon.location.sample.util.CONFIGURATION
import software.amazon.location.sample.util.DELAY_10000
import software.amazon.location.sample.util.DELAY_15000
import software.amazon.location.sample.util.DELAY_3000
import software.amazon.location.sample.util.DELAY_5000
import software.amazon.location.sample.util.OUTPUT_MESSAGE
import software.amazon.location.sample.util.SIGN_IN
import software.amazon.location.sample.util.SIGN_OUT
import software.amazon.location.sample.util.STOP_TRACKING_IN_BACKGROUND
import software.amazon.location.sample.util.SWITCH_TIME_FILTER
import software.amazon.location.sample.util.TRACKING
import software.amazon.location.sample.util.grantPermission
import software.amazon.location.sample.util.signIn

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityStartStopBackgroundTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun startStopTrackingBackground() {
        val btnSignIn = uiDevice.findObject(UiSelector().text(SIGN_IN))
        if (btnSignIn.exists()) {
            signIn(composeTestRule)
        }
        grantPermission("android.permission.POST_NOTIFICATIONS")
        grantPermission("android.permission.ACCESS_COARSE_LOCATION")
        grantPermission("android.permission.ACCESS_FINE_LOCATION")
        grantPermission("android.permission.ACCESS_BACKGROUND_LOCATION")
        uiDevice.executeShellCommand("logcat -c")
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        Thread.sleep(DELAY_5000)
        composeTestRule.onNodeWithText(TRACKING).performClick()
        composeTestRule.onNodeWithTag(BTN_TRACKING_BACKGROUND).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BTN_TRACKING_BACKGROUND).performClick()

        Thread.sleep(DELAY_15000)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(DELAY_15000) {
            uiDevice.findObject(By.text(STOP_TRACKING_IN_BACKGROUND)) != null
        }
        composeTestRule.onNodeWithTag(BTN_TRACKING_BACKGROUND).performClick()
        Thread.sleep(DELAY_5000)
        composeTestRule.waitForIdle()
        val logcatOutput: String = uiDevice.executeShellCommand("logcat -d").trim()
        assertTrue(
            OUTPUT_MESSAGE,
            logcatOutput.contains("Fetched location")
        )
    }

    @After
    fun signOut() {
        composeTestRule.onNodeWithText(CONFIGURATION).performClick()
        Thread.sleep(DELAY_5000)
        composeTestRule.waitForIdle()
        val btnSignOut = uiDevice.findObject(UiSelector().text(SIGN_OUT))
        if (btnSignOut.exists()) {
            composeTestRule.onNodeWithText(SIGN_OUT).performClick()
            Thread.sleep(DELAY_3000)
            composeTestRule.waitForIdle()
        }
    }
}
