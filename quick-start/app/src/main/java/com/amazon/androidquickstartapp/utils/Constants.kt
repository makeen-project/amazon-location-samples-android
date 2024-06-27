package com.amazon.androidquickstartapp.utils

import com.google.android.gms.location.Priority

object Constants {
    const val SERVICE_NAME = "geo"
    const val TRACKER_LINE_SOURCE = "tracker_line_source"
    const val TRACKER_LINE_LAYER = "tracker_line_layer"
    const val FREQUENCY = 1000L
    const val ACCURACY = Priority.PRIORITY_HIGH_ACCURACY
    const val LATENCY = 1000L
    const val WAIT_FOR_ACCURATE_LOCATION = false
    const val MIN_UPDATE_INTERVAL_MILLIS = 1000L
    const val TRANSITION_DURATION = 1200L
    const val REVERSE_GEO_THRESH_HOLD = 1000
    const val TRACKING_FREQUENCY_MILLIS = 5000L
    const val TRACKING_MIN_UPDATE_INTERVAL_MILLIS = 5000L
}