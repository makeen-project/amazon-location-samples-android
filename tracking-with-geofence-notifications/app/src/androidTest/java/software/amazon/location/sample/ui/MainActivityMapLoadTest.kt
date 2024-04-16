package software.amazon.location.sample.ui

import androidx.compose.ui.test.assertIsDisplayed
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import software.amazon.location.sample.util.DELAY_10000
import software.amazon.location.sample.util.DELAY_3000
import software.amazon.location.sample.MainActivity
import software.amazon.location.sample.util.DELAY_1000
import software.amazon.location.sample.util.MAP_VIEW
import software.amazon.location.sample.util.SIGN_IN
import software.amazon.location.sample.util.SIGN_OUT
import software.amazon.location.sample.util.signIn

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityMapLoadTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun mapLoadTest() {
        val btnSignIn = uiDevice.findObject(UiSelector().text(SIGN_IN))
        if (btnSignIn.exists()) {
            signIn(composeTestRule)
        }

        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(MAP_VIEW).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        Thread.sleep(DELAY_3000)
        composeTestRule.onNodeWithTag(MAP_VIEW)
            .assertIsDisplayed()
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
