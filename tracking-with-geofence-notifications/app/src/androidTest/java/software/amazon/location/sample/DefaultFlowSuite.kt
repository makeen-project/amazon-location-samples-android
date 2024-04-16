package software.amazon.location.sample

import org.junit.runner.RunWith
import org.junit.runners.Suite
import software.amazon.location.sample.ui.MainActivityBatterySaverTrackingTest
import software.amazon.location.sample.ui.MainActivityFilterEnableDisableTest
import software.amazon.location.sample.ui.MainActivityFilterUpdateTest
import software.amazon.location.sample.ui.MainActivityMapLoadTest
import software.amazon.location.sample.ui.MainActivityPermissionTest
import software.amazon.location.sample.ui.MainActivitySignInTest
import software.amazon.location.sample.ui.MainActivityStartStopBackgroundTest
import software.amazon.location.sample.ui.MainActivityStartStopForegroundTest
import software.amazon.location.sample.ui.MainActivitySwitchToTrackingTest

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MainActivitySignInTest::class,
    MainActivityFilterUpdateTest::class,
    MainActivityFilterEnableDisableTest::class,
    MainActivityMapLoadTest::class,
    MainActivitySwitchToTrackingTest::class,
    MainActivityPermissionTest::class,
    MainActivityStartStopForegroundTest::class,
    MainActivityStartStopBackgroundTest::class,
    MainActivityBatterySaverTrackingTest::class,
)
class DefaultFlowSuite
