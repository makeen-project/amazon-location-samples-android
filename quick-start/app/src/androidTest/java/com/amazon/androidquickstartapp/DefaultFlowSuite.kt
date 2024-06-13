package com.amazon.androidquickstartapp

import com.amazon.androidquickstartapp.ui.MainActivityLocateMeTest
import com.amazon.androidquickstartapp.ui.MainActivityMapLoadTest
import com.amazon.androidquickstartapp.ui.MainActivityMapSwipeTest
import com.amazon.androidquickstartapp.ui.MainActivityPermissionTest
import com.amazon.androidquickstartapp.ui.MainActivityReverseGeocodeTest
import com.amazon.androidquickstartapp.ui.MainActivityStartStopTrackingTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MainActivityMapLoadTest::class,
    MainActivityReverseGeocodeTest::class,
    MainActivityMapSwipeTest::class,
    MainActivityPermissionTest::class,
    MainActivityLocateMeTest::class,
    MainActivityStartStopTrackingTest::class,
)
class DefaultFlowSuite
