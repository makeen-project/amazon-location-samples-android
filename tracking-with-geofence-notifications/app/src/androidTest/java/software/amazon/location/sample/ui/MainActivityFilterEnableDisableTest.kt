package software.amazon.location.sample.ui


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
import software.amazon.location.sample.util.ICON_DISTANCE_PLUS
import software.amazon.location.sample.util.ICON_TIME_PLUS
import software.amazon.location.sample.util.SIGN_IN
import software.amazon.location.sample.util.SIGN_OUT
import software.amazon.location.sample.util.SWITCH_ACCURACY_FILTER
import software.amazon.location.sample.util.SWITCH_DISTANCE_FILTER
import software.amazon.location.sample.util.SWITCH_TIME_FILTER
import software.amazon.location.sample.util.signIn

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityFilterEnableDisableTest {

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
    fun timeFilterEnableDisable() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).performClick()
        composeTestRule.onNodeWithTag(ICON_TIME_PLUS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).performClick()
        composeTestRule.onNodeWithTag(SWITCH_TIME_FILTER).assertIsEnabled()
    }

    @Test
    fun distanceFilterEnableDisable() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).performClick()
        composeTestRule.onNodeWithTag(ICON_DISTANCE_PLUS).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).performClick()
        composeTestRule.onNodeWithTag(SWITCH_DISTANCE_FILTER).assertIsEnabled()
    }

    @Test
    fun accuracyFilterEnableDisable() {
        composeTestRule.waitUntil(DELAY_10000) {
            val isVisible = try {
                composeTestRule.onNodeWithTag(SWITCH_ACCURACY_FILTER).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
            isVisible
        }
        composeTestRule.onNodeWithTag(SWITCH_ACCURACY_FILTER).performClick()
        composeTestRule.onNodeWithTag(SWITCH_ACCURACY_FILTER).assertIsEnabled()
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
