package com.amazon.androidquickstartapp

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.amazon.androidquickstartapp.ui.theme.AndroidQuickStartAppTheme
import com.amazon.androidquickstartapp.ui.view.MapLoadScreen
import com.amazon.androidquickstartapp.ui.viewModel.MainViewModel
import com.amazon.androidquickstartapp.utils.Constants.REVERSE_GEO_THRESH_HOLD
import com.amazon.androidquickstartapp.utils.Constants.SERVICE_NAME
import com.amazon.androidquickstartapp.utils.Constants.TRACKER_LINE_LAYER
import com.amazon.androidquickstartapp.utils.Constants.TRACKER_LINE_SOURCE
import com.amazon.androidquickstartapp.utils.Constants.TRACKING_FREQUENCY_MILLIS
import com.amazon.androidquickstartapp.utils.Constants.TRACKING_MIN_UPDATE_INTERVAL_MILLIS
import com.amazon.androidquickstartapp.utils.DialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.geojson.Point
import software.amazon.location.auth.AuthHelper
import software.amazon.location.auth.AwsSignerInterceptor
import software.amazon.location.tracking.aws.LocationTrackingCallback
import software.amazon.location.tracking.config.LocationTrackerConfig
import software.amazon.location.tracking.database.LocationEntry
import software.amazon.location.tracking.filters.DistanceLocationFilter
import software.amazon.location.tracking.filters.TimeLocationFilter
import software.amazon.location.tracking.util.TrackingSdkLogLevel

class MainActivity : ComponentActivity(), OnMapReadyCallback, MapLibreMap.OnCameraIdleListener,
    MapLibreMap.OnCameraMoveStartedListener {

    private lateinit var authHelper: AuthHelper
    private val mainViewModel: MainViewModel by viewModels()
    private val coroutineScope = MainScope()
    private var isRequestingForTracking = false
    private var dialogHelper: DialogHelper? = null
    private var coordinates = arrayListOf<Point>()
    private var cameraIdleTime: Long = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions: Map<String, Boolean> ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            if (isRequestingForTracking) {
                startStopTracking()
            } else {
                mainViewModel.enableLocationComponent(this)
            }
        } else {
            val shouldShowRationale = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (!shouldShowRationale) {
                if (dialogHelper == null) {
                    dialogHelper = DialogHelper(this)
                }
                dialogHelper?.showSettingsDialog(false, onOpenAppSettings = {
                    mainViewModel.openAppSettings(this)
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this)
        super.onCreate(savedInstanceState)
        setContent {
            AndroidQuickStartAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (mainViewModel.authenticated) {
                        MapLoadScreen(
                            mainViewModel,
                            mapReadyCallback = this,
                            onLocateMeClick = {
                                if (mainViewModel.checkLocationPermission(this@MainActivity)) {
                                    isRequestingForTracking = false
                                    val permissions = arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                    requestPermissionLauncher.launch(permissions)
                                } else {
                                    if (mainViewModel.locationComponent == null) {
                                        mainViewModel.enableLocationComponent(this)
                                    }
                                    mainViewModel.setLiveTracking()
                                }
                            },
                            onStartStopTrackingClick = {
                                if (mainViewModel.checkLocationPermission(this@MainActivity)) {
                                    isRequestingForTracking = true
                                    mainViewModel.helper.showToast(
                                        getString(R.string.location_permission_not_granted_requesting_permission),
                                        this@MainActivity
                                    )
                                    val permissions = arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                    requestPermissionLauncher.launch(permissions)
                                } else {
                                    startStopTracking()
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Black)
                        }
                    }
                }
            }
        }
        signInUser()
    }

    private fun startStopTracking() {
        if (mainViewModel.locationComponent == null) {
            mainViewModel.enableLocationComponent(this)
        }
        if (mainViewModel.isLocationTrackingForegroundActive) {
            mainViewModel.stopTrackingForeground(applicationContext)
        } else {
            mainViewModel.setLiveTracking()
            mainViewModel.startTrackingForeground(
                this@MainActivity,
                locationTrackingCallback = object :
                    LocationTrackingCallback {
                    override fun onLocationAvailabilityChanged(
                        locationAvailable: Boolean
                    ) {
                    }

                    override fun onLocationReceived(location: LocationEntry) {
                        runOnUiThread {
                            if (coordinates.size == 0) {
                                mainViewModel.mapLibreMap?.getStyle { style ->
                                    mainViewModel.layerSize = style.layers.size
                                }
                            }
                            coordinates.add(
                                Point.fromLngLat(
                                    location.longitude,
                                    location.latitude
                                )
                            )

                            if (coordinates.size > 1) {
                                mainViewModel.mapLibreMap?.let {
                                    mainViewModel.mapHelper.addTrackerLine(
                                        coordinates,
                                        TRACKER_LINE_LAYER,
                                        TRACKER_LINE_SOURCE,
                                        this@MainActivity,
                                        it,
                                        mainViewModel.layerSize
                                    )
                                }
                            }
                        }
                    }

                    override fun onUploadSkipped(entries: LocationEntry) {
                        runOnUiThread {
                            mainViewModel.mapLibreMap?.let { it1 ->
                                mainViewModel.mapHelper.addMarker(
                                    it1,
                                    LatLng(
                                        entries.latitude,
                                        entries.longitude
                                    ),
                                    R.drawable.ic_tracker_skipped,
                                    "marker-layer-skipped${entries.id}",
                                    "marker-source-skipped${entries.id}",
                                    "image-skipped${entries.id}",
                                    this@MainActivity
                                )
                            }
                        }
                    }

                    override fun onUploadStarted(entries: List<LocationEntry>) {
                    }

                    override fun onUploaded(entries: List<LocationEntry>) {
                        runOnUiThread {
                            entries.forEach {
                                mainViewModel.mapLibreMap?.let { it1 ->
                                    mainViewModel.mapHelper.addMarker(
                                        it1,
                                        LatLng(
                                            it.latitude,
                                            it.longitude
                                        ),
                                        R.drawable.ic_tracker,
                                        "marker-layer${it.id}",
                                        "marker-source${it.id}",
                                        "image-upload${it.id}",
                                        this@MainActivity
                                    )
                                }
                            }
                        }
                    }

                })
        }
    }

    /**
     * Signs in the user by authenticating with the identityPoolId
     */
    private fun signInUser() {
        coroutineScope.launch {
            if (mainViewModel.checkValidations(this@MainActivity)) return@launch
            authHelper = AuthHelper(applicationContext)
            mainViewModel.initializeLocationCredentialsProvider(authHelper)
            mainViewModel.authenticated = true
            mainViewModel.locationCredentialsProvider?.let {
                HttpRequestUtil.setOkHttpClient(
                    OkHttpClient.Builder()
                        .addInterceptor(
                            AwsSignerInterceptor(
                                SERVICE_NAME,
                                mainViewModel.region,
                                it
                            )
                        )
                        .build()
                )
                val config = LocationTrackerConfig(
                    trackerName = mainViewModel.trackerName,
                    logLevel = TrackingSdkLogLevel.DEBUG,
                    frequency = TRACKING_FREQUENCY_MILLIS,
                    waitForAccurateLocation = false,
                    minUpdateIntervalMillis = TRACKING_MIN_UPDATE_INTERVAL_MILLIS,
                )
                mainViewModel.initializeLocationTracker(applicationContext, it, config)
                mainViewModel.locationTracker?.enableFilter(TimeLocationFilter())
                mainViewModel.locationTracker?.enableFilter(DistanceLocationFilter())
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mainViewModel.mapLibreMap = map
        map.setStyle(
            Style.Builder()
                .fromUri(
                    "https://maps.geo.${mainViewModel.region}.amazonaws.com/maps/v0/maps/${mainViewModel.mapName}/style-descriptor"
                ),
        ) {
            map.uiSettings.isAttributionEnabled = true
            map.uiSettings.isLogoEnabled = false
            map.uiSettings.attributionGravity = Gravity.BOTTOM or Gravity.END
            val initialPosition = LatLng(47.6160281982247, -122.32642111977668)
            map.cameraPosition = CameraPosition.Builder()
                .target(initialPosition)
                .zoom(14.0)
                .build()
            val location = Location("")
            location.latitude = initialPosition.latitude
            location.longitude = initialPosition.longitude
            mainViewModel.lastLocation = location

            mainViewModel.mapLibreMap?.addOnCameraIdleListener(this)
            mainViewModel.mapLibreMap?.addOnCameraMoveStartedListener(this)
            cameraIdleTime = System.currentTimeMillis()
            map.cameraPosition.target?.let { latLng ->
                getLabelFromPosition(latLng)
            }
        }
    }

    private fun getLabelFromPosition(latLng: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            val label = mainViewModel.reverseGeocode(
                LatLng(
                    latLng.latitude,
                    latLng.longitude
                )
            )
            label?.let {
                mainViewModel.label = it
            }
        }
    }

    override fun onCameraIdle() {
        if ((System.currentTimeMillis() - cameraIdleTime) <= REVERSE_GEO_THRESH_HOLD) {
            return
        }
        cameraIdleTime = System.currentTimeMillis()
        mainViewModel.mapLibreMap?.cameraPosition?.target?.let { latLng ->
            getLabelFromPosition(latLng)
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
            mainViewModel.isFollowingLocationMarker = false
        }
    }
}