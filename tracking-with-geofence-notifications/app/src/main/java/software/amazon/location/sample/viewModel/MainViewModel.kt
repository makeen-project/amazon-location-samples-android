package software.amazon.location.sample.viewModel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import software.amazon.location.auth.EncryptedSharedPreferences
import software.amazon.location.auth.LocationCredentialsProvider
import software.amazon.location.sample.BuildConfig
import software.amazon.location.sample.utils.Constant.PREFS_NAME_TRACKING
import software.amazon.location.tracking.LocationTracker
import software.amazon.location.tracking.aws.LocationTrackingCallback
import software.amazon.location.tracking.config.LocationTrackerConfig
import software.amazon.location.tracking.database.LocationEntry
import software.amazon.location.tracking.filters.AccuracyLocationFilter
import software.amazon.location.tracking.filters.DistanceLocationFilter
import software.amazon.location.tracking.filters.LocationFilter
import software.amazon.location.tracking.filters.LocationFilterAdapter
import software.amazon.location.tracking.filters.TimeLocationFilter
import software.amazon.location.tracking.providers.BackgroundLocationService
import software.amazon.location.tracking.providers.BackgroundTrackingWorker
import software.amazon.location.tracking.util.BackgroundTrackingMode
import software.amazon.location.tracking.util.Logger
import software.amazon.location.tracking.util.ServiceCallback
import software.amazon.location.tracking.util.StoreKey

class MainViewModel : ViewModel() {
    var locationTracker: LocationTracker? = null
    var authenticated by mutableStateOf(false)
    var isLocationTrackingForegroundActive: Boolean by mutableStateOf(false)
    var isLocationTrackingBackgroundActive: Boolean by mutableStateOf(false)
    var isLocationTrackingBatteryOptimizeActive: Boolean by mutableStateOf(false)
    var isLoading: Boolean by mutableStateOf(false)
    var identityPoolId by mutableStateOf("")
    var trackerName by mutableStateOf("")
    var mapName by mutableStateOf("")
    var timeFilterEnabled: Boolean by mutableStateOf(false)
    var distanceFilterEnabled: Boolean by mutableStateOf(false)
    var accuracyFilterEnabled: Boolean by mutableStateOf(false)
    var timeInterval: Long by mutableLongStateOf(20)
    var distanceThreshold: Double by mutableDoubleStateOf(20.0)
    var isIncrementing: Boolean by mutableStateOf(false)
    var isDecrementing: Boolean by mutableStateOf(false)
    var lastAccuracyMeasured by mutableStateOf("")
    var isConfigDialogVisible: Boolean by mutableStateOf(false)
    var selectedTrackingMode = BackgroundTrackingMode.ACTIVE_TRACKING
    var enableGeofences = false
    var locationCredentialsProvider: LocationCredentialsProvider? = null

    fun setDistanceFilterData() {
        locationTracker?.checkFilterIsExistsAndUpdateValue(
            DistanceLocationFilter(distanceThreshold),
        )
    }

    fun setTimeFilterData() {
        locationTracker?.checkFilterIsExistsAndUpdateValue(
            TimeLocationFilter(timeInterval * 1000),
        )
    }

    fun stopBackgroundService() {
        locationTracker?.stopBackgroundService()
        if (selectedTrackingMode == BackgroundTrackingMode.ACTIVE_TRACKING) {
            isLocationTrackingBackgroundActive = false
        } else {
            isLocationTrackingBatteryOptimizeActive = false
        }
    }

    fun checkBackgroundServiceRunning(applicationContext: Context) {
        if (authenticated) {
            if (BackgroundLocationService.isRunning) {
                isLocationTrackingBackgroundActive = true
            }
            if (BackgroundTrackingWorker.isWorkRunning(applicationContext)) {
                isLocationTrackingBatteryOptimizeActive = true
            }
        } else {
            if (BackgroundLocationService.isRunning) {
                val intent = Intent(applicationContext, BackgroundLocationService::class.java)
                applicationContext.stopService(intent)
            }
            if (BackgroundTrackingWorker.isWorkRunning(applicationContext)) {
                BackgroundTrackingWorker.cancelWork(applicationContext)
            }
        }
    }

    fun startBackgroundTracking() {
        locationTracker?.startBackground(
            selectedTrackingMode,
            object : ServiceCallback {
                override fun serviceStopped() {
                    if (selectedTrackingMode == BackgroundTrackingMode.ACTIVE_TRACKING) {
                        isLocationTrackingBackgroundActive = false
                    } else {
                        isLocationTrackingBatteryOptimizeActive = false
                    }
                }
            },
        )
        if (selectedTrackingMode == BackgroundTrackingMode.ACTIVE_TRACKING) {
            isLocationTrackingBackgroundActive = true
        } else {
            isLocationTrackingBatteryOptimizeActive = true
        }
    }

    suspend fun evaluateGeofence(
        locationEntry: List<LocationEntry>,
        deviceId: String,
        identityId: String
    ): Boolean {
        return try {
            val response = locationTracker?.batchEvaluateGeofences(locationEntry, deviceId, identityId, BuildConfig.GEOFENCE_COLLECTION_NAME)
            response != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setUserAuthenticated() {
        isLoading = false
        authenticated = true
        isConfigDialogVisible = false
    }

    fun checkFilterData(context: Context) {
        val locationTrackerConfig = GsonBuilder()
            .registerTypeAdapter(LocationFilter::class.java, LocationFilterAdapter())
            .create().fromJson(
                EncryptedSharedPreferences(
                    context,
                    PREFS_NAME_TRACKING,
                ).apply { initEncryptedSharedPreferences() }.get(StoreKey.CLIENT_CONFIG)?: throw Exception("Client config not found"),
                LocationTrackerConfig::class.java,
            )
        locationTrackerConfig?.let { config ->
            config.locationFilters.forEach {
                locationTracker?.enableFilter(it)
                when (it) {
                    is TimeLocationFilter -> {
                        timeFilterEnabled = true
                        timeInterval = it.timeInterval / 1000
                    }

                    is DistanceLocationFilter -> {
                        distanceFilterEnabled = true
                        distanceThreshold = it.distanceThreshold
                    }

                    is AccuracyLocationFilter -> {
                        accuracyFilterEnabled = true
                    }
                }
            }
        }
    }

    /**
     * Subscribes to location updates and updates the UI button text accordingly.
     */
    fun startTrackingForeground(
        context: Context,
        locationTrackingCallback: LocationTrackingCallback,
        onRequestPermissionsResultCallback: () -> Unit
    ) {
        Logger.log("Checking location permission")
        if (checkLocationPermission(context)) {
            Logger.log("Location permission not granted. Requesting permission")
            onRequestPermissionsResultCallback()
            return
        }
        isLocationTrackingForegroundActive = true
        locationTracker?.start(locationTrackingCallback)
    }

    /**
     * Unsubscribes from location updates and updates the UI button text accordingly.
     */
    fun stopTrackingForeground(context: Context, onRequestPermissionsResultCallback: () -> Unit) {
        Logger.log("Checking location permission")
        if (checkLocationPermission(context)) {
            Logger.log("Location permission not granted. Requesting permission")
            onRequestPermissionsResultCallback()
            return
        }
        isLocationTrackingForegroundActive = false
        locationTracker?.stop()
    }

    fun checkLocationPermission(context: Context) = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) != PackageManager.PERMISSION_GRANTED
}