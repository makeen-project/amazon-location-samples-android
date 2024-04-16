package software.amazon.location.sample.util

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import software.amazon.location.sample.BuildConfig
import software.amazon.location.sample.MainActivity

fun signIn(composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>) {
    composeTestRule.onNodeWithText(SIGN_IN).performClick()
    Thread.sleep(DELAY_3000)
    composeTestRule.onNodeWithText(IDENTITY_POOL_ID).assertIsDisplayed()
    Thread.sleep(DELAY_1000)
    composeTestRule.onNodeWithText(IDENTITY_POOL_ID)
        .performClick()
        .performTextReplacement(BuildConfig.TEST_POOL_ID)
    composeTestRule.onNodeWithText(TRACKER_NAME)
        .performClick()
        .performTextInput(BuildConfig.DEFAULT_TRACKER_NAME)
    composeTestRule.onNodeWithText(MAP_NAME)
        .performClick()
        .performTextInput(BuildConfig.TEST_MAP_NAME)
    composeTestRule.onNodeWithTag(BTN_SIGN_IN).performClick()
    composeTestRule.waitForIdle()
}

fun grantPermission(permission: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val uiDevice = UiDevice.getInstance(instrumentation)

    uiDevice.executeShellCommand("pm grant ${instrumentation.targetContext.packageName} $permission")
}