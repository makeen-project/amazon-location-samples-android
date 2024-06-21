package software.amazon.location.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Priority
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
import software.amazon.location.auth.AuthHelper
import software.amazon.location.auth.AwsSignerInterceptor
import software.amazon.location.auth.EncryptedSharedPreferences
import software.amazon.location.sample.helper.DialogHelper
import software.amazon.location.sample.helper.Helper
import software.amazon.location.sample.helper.MqttHelper
import software.amazon.location.sample.theme.AmazonTrackingSDKSampleAppTheme
import software.amazon.location.sample.utils.Constant
import software.amazon.location.sample.view.ConfigurationScreen
import software.amazon.location.sample.view.TrackingScreen
import software.amazon.location.sample.viewModel.MainViewModel
import software.amazon.location.tracking.aws.LocationTrackingCallback
import software.amazon.location.tracking.config.LocationTrackerConfig
import software.amazon.location.tracking.config.NotificationConfig
import software.amazon.location.tracking.database.LocationEntry
import software.amazon.location.tracking.providers.BackgroundLocationService
import software.amazon.location.tracking.providers.BackgroundTrackingWorker
import software.amazon.location.tracking.providers.DeviceIdProvider
import software.amazon.location.tracking.util.BackgroundTrackingMode
import software.amazon.location.tracking.util.Logger
import software.amazon.location.tracking.util.TrackingSdkLogLevel

class MainActivity : ComponentActivity(), LocationTrackingCallback, OnMapReadyCallback {

    private lateinit var encryptedSharedPreferences: EncryptedSharedPreferences
    private lateinit var authHelper: AuthHelper
    private lateinit var deviceIdProvider: DeviceIdProvider
    private val coroutineScope = MainScope()
    private var isForBackgroundPermissionAsked: Boolean = false
    private var mqttHelper: MqttHelper? = null
    private var dialogHelper: DialogHelper? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var helper = Helper()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions: Map<String, Boolean> ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Logger.log("Permission granted")

            coroutineScope.launch {
                if (isForBackgroundPermissionAsked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission()
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val location = mainViewModel.locationTracker?.getDeviceLocation(null)
                        Logger.log("Got location: ${location?.longitude}, ${location?.latitude}")
                    }
                }
            }
        } else {
            val shouldShowRationale = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (!shouldShowRationale) {
                dialogHelper?.showSettingsDialog(false, onOpenAppSettings = {
                    openAppSettings()
                })
            }
            Logger.log("Permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permission: Boolean ->
        if (permission) {
            if (mainViewModel.selectedTrackingMode == BackgroundTrackingMode.ACTIVE_TRACKING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Logger.log("Background permission granted")
                mainViewModel.stopTrackingForeground(this, onRequestPermissionsResultCallback = {
                    requestLocationPermission(false)
                })
                mainViewModel.startBackgroundTracking()
            }
        } else {
            val shouldShowRationale = arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ).any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (!shouldShowRationale) {
                dialogHelper?.showSettingsDialog(true, onOpenAppSettings = {
                    openAppSettings()
                })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permission: Boolean ->
        if (permission) {
            Logger.log("Notification permission granted")
            if (isForBackgroundPermissionAsked) {
                Logger.log(getString(R.string.label_all_permission_granted))
                mainViewModel.stopTrackingForeground(this, onRequestPermissionsResultCallback = {
                    requestLocationPermission(false)
                })
                mainViewModel.startBackgroundTracking()
            } else {
                coroutineScope.launch {
                    val location = mainViewModel.locationTracker?.getDeviceLocation(null)
                    Logger.log("Got location: ${location?.longitude}, ${location?.latitude}")
                }
            }
        } else {
            val shouldShowRationale = arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
            ).any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (!shouldShowRationale) {
                dialogHelper?.showSettingsForNotificationDialog(onOpenAppSettings = {
                    openAppSettings()
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this)
        super.onCreate(savedInstanceState)
        deviceIdProvider = DeviceIdProvider(applicationContext)
        authHelper = AuthHelper(applicationContext)
        dialogHelper = DialogHelper(this)
        mainViewModel.enableGeofences = BuildConfig.MQTT_END_POINT.isNotEmpty()
                && BuildConfig.POLICY_NAME.isNotEmpty()
                && BuildConfig.TOPIC_TRACKER.isNotEmpty()
        Logger.log("enableGeofences: ${mainViewModel.enableGeofences}")
        setContent {
            AmazonTrackingSDKSampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen()
                }
            }
        }
        checkStatus()
        mainViewModel.checkBackgroundServiceRunning(applicationContext)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainScreen() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        Scaffold(
            content = {
                Column {
                    when (selectedTabIndex) {
                        0 -> Box(
                            modifier = Modifier.weight(1f),
                        ) {
                            ConfigurationScreen(
                                mainViewModel,
                                this@MainActivity,
                                this@MainActivity,
                                onSignInFromConfigurationClicked = {
                                    Logger.log("Signing in")
                                    signInUser(false)
                                },
                                onSignInClicked = {
                                    if (!mainViewModel.authenticated) {
                                        mainViewModel.isConfigDialogVisible = true
                                    } else {
                                        signOutUser()
                                    }
                                })
                        }

                        1 -> Box(
                            modifier = Modifier.weight(1f),
                        ) {
                            TrackingScreen(mainViewModel, onStartTrackingForeground = {
                                if (mainViewModel.locationTracker != null) {
                                    if (!mainViewModel.isLocationTrackingForegroundActive) {
                                        mainViewModel.stopBackgroundService()
                                        mainViewModel.startTrackingForeground(
                                            this@MainActivity,
                                            this@MainActivity,
                                            onRequestPermissionsResultCallback = {
                                                requestLocationPermission(false)
                                            })
                                    } else {
                                        mainViewModel.stopTrackingForeground(
                                            this@MainActivity,
                                            onRequestPermissionsResultCallback = {
                                                requestLocationPermission(false)
                                            })
                                    }
                                } else {
                                    Logger.log("Please sign in first")
                                }
                            }, onStartTrackingBackground = {
                                if (mainViewModel.locationTracker != null) {
                                    if (BackgroundTrackingWorker.isWorkRunning(applicationContext)) {
                                        mainViewModel.stopBackgroundService()
                                    }
                                    mainViewModel.selectedTrackingMode =
                                        BackgroundTrackingMode.ACTIVE_TRACKING
                                    if (!BackgroundLocationService.isRunning) {
                                        checkPermissionAndStartBackgroundService()
                                    } else {
                                        mainViewModel.stopBackgroundService()
                                    }
                                } else {
                                    Logger.log("Please sign in first")
                                }
                            }, onStartTrackingBatteryOptimization = {
                                if (mainViewModel.locationTracker != null) {
                                    if (BackgroundLocationService.isRunning) {
                                        mainViewModel.stopBackgroundService()
                                    }
                                    mainViewModel.selectedTrackingMode =
                                        BackgroundTrackingMode.BATTERY_SAVER_TRACKING
                                    if (!BackgroundTrackingWorker.isWorkRunning(applicationContext)) {
                                        checkPermissionAndStartBackgroundService()
                                    } else {
                                        mainViewModel.stopBackgroundService()
                                    }
                                } else {
                                    Logger.log("Please sign in first")
                                }
                            })
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primary),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            )
                        },
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                        ) {
                            Text(
                                stringResource(R.string.tab_configuration),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            enabled = mainViewModel.authenticated,
                        ) {
                            Text(
                                stringResource(R.string.tab_tracking),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }
            },
        )
    }

    private fun checkStatus() {
        val sharedPreferences = getSharedPreferences(Constant.PREFS_NAME, MODE_PRIVATE)
        val authenticated = sharedPreferences.getBoolean(Constant.KEY_AUTHENTICATED, false)
        if (authenticated) {
            signInUser(true)
            mainViewModel.locationTracker?.let {
                if (it.isTrackingInForeground()) {
                    mainViewModel.startTrackingForeground(
                        this,
                        this,
                        onRequestPermissionsResultCallback = {
                            requestLocationPermission(false)
                        })
                }
            }
        }
    }

    /**
     * Signs in the user by authenticating with the Cognito Identity Pool
     */
    private fun signInUser(isAlreadySignedIn: Boolean) {
        coroutineScope.launch {
            encryptedSharedPreferences =
                EncryptedSharedPreferences(this@MainActivity, Constant.PREFS_NAME_AUTH)
            encryptedSharedPreferences.initEncryptedSharedPreferences()
            if (isAlreadySignedIn) {
                mainViewModel.identityPoolId =
                    encryptedSharedPreferences.get(Constant.PREFS_KEY_IDENTITY_POOL_ID) ?: ""
                mainViewModel.trackerName =
                    encryptedSharedPreferences.get(Constant.PREFS_KEY_TRACKER_NAME) ?: ""
                mainViewModel.mapName =
                    encryptedSharedPreferences.get(Constant.PREFS_KEY_MAP_NAME) ?: ""
            }
            if (mainViewModel.identityPoolId.isEmpty()) {
                helper.showToast(
                    getString(R.string.error_please_enter_identity_pool_id),
                    this@MainActivity
                )
                return@launch
            }
            if (mainViewModel.trackerName.isEmpty()) {
                helper.showToast(
                    getString(R.string.error_please_enter_tracker_name),
                    this@MainActivity
                )
                return@launch
            }
            if (mainViewModel.mapName.isEmpty()) {
                helper.showToast(getString(R.string.error_please_enter_map_name), this@MainActivity)
                return@launch
            }
            encryptedSharedPreferences.put(
                Constant.PREFS_KEY_TRACKER_NAME,
                mainViewModel.trackerName
            )
            encryptedSharedPreferences.put(Constant.PREFS_KEY_MAP_NAME, mainViewModel.mapName)
            mainViewModel.isLoading = true
            mainViewModel.initializeLocationCredentialsProvider(authHelper)
            mainViewModel.setUserAuthenticated()
            mainViewModel.locationCredentialsProvider?.let {
                HttpRequestUtil.setOkHttpClient(
                    OkHttpClient.Builder()
                        .addInterceptor(
                            AwsSignerInterceptor(
                                Constant.SERVICE_NAME,
                                mainViewModel.identityPoolId.split(":")[0],
                                it
                            )
                        )
                        .build()
                )
                val config = LocationTrackerConfig(
                    trackerName = mainViewModel.trackerName,
                    logLevel = TrackingSdkLogLevel.DEBUG,
                    accuracy = Priority.PRIORITY_HIGH_ACCURACY,
                    latency = 1000,
                    frequency = 5000,
                    waitForAccurateLocation = false,
                    minUpdateIntervalMillis = 5000,
                    persistentNotificationConfig = NotificationConfig(
                        notificationImageId = R.drawable.ic_drive,
                    ),
                )
                mainViewModel.initializeLocationTracker(applicationContext, it, config)
                Logger.log("Signed in")
                Logger.log("Device ID: ${deviceIdProvider.getDeviceID()}")
                val sharedPreferences =
                    getSharedPreferences(Constant.PREFS_NAME, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(Constant.KEY_AUTHENTICATED, true)
                    .apply()
                initMqtt()
                mainViewModel.checkFilterData(this@MainActivity)
            }
        }
    }

    private fun initMqtt() {
        CoroutineScope(Dispatchers.Default).launch {
            if (!mainViewModel.enableGeofences) {
                return@launch
            }
            val identityId = encryptedSharedPreferences.get(Constant.PREFS_KEY_IDENTITY_ID) ?: ""
            mqttHelper = MqttHelper(
                applicationContext,
                mainViewModel.locationCredentialsProvider?.getCredentialsProvider(),
                identityId
            )
            mqttHelper?.setIotPolicy()
            mqttHelper?.startMqttManager()
        }
    }

    /**
     * Signs out the user by clearing location credentials, resetting the location client,
     * updating authentication status, and resetting UI elements.
     */
    private fun signOutUser() {
        mainViewModel.locationCredentialsProvider?.clear()
        mainViewModel.locationTracker = null

        mainViewModel.authenticated = false
        deviceIdProvider.resetDeviceID()
        val sharedPreferences = getSharedPreferences(Constant.PREFS_NAME, MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(Constant.KEY_AUTHENTICATED, false).apply()
        CoroutineScope(Dispatchers.Default).launch {
            if (!mainViewModel.enableGeofences) {
                return@launch
            }
            mqttHelper?.stopMqttManager()
        }
    }

    private fun requestLocationPermission(isForBackgroundPermissionAsked: Boolean) {
        this.isForBackgroundPermissionAsked = isForBackgroundPermissionAsked
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        requestPermissionLauncher.launch(permissions)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun checkPermissionAndStartBackgroundService() {
        if (mainViewModel.checkLocationPermission(this)) {
            requestLocationPermission(true)
            return
        } else {
            isForBackgroundPermissionAsked = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission()
            } else {
                Logger.log(getString(R.string.label_all_permission_granted))
                mainViewModel.stopTrackingForeground(this, onRequestPermissionsResultCallback = {
                    requestLocationPermission(false)
                })
                mainViewModel.startBackgroundTracking()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun GreetingPreview() {
        AmazonTrackingSDKSampleAppTheme {
            MainScreen()
        }
    }

    override fun onLocationReceived(location: LocationEntry) {
        Logger.log("getDeviceLocation onLocationReceived")
    }

    override fun onUploadStarted(entries: List<LocationEntry>) {
        Logger.log("getDeviceLocation onUploadStarted")
    }

    override fun onUploaded(entries: List<LocationEntry>) {
        Logger.log("getDeviceLocation onUploaded")
        CoroutineScope(Dispatchers.Default).launch {
            if (!mainViewModel.enableGeofences) {
                return@launch
            }
            val identityId = encryptedSharedPreferences.get(Constant.PREFS_KEY_IDENTITY_ID) ?: ""
            mainViewModel.evaluateGeofence(entries, helper.getDeviceId(applicationContext), identityId)
        }
        if (mainViewModel.accuracyFilterEnabled && entries.isNotEmpty()) {
            mainViewModel.lastAccuracyMeasured =
                entries.first().accuracy.toString() + " " + getString(R.string.label_meter)
        }
    }

    override fun onUploadSkipped(entries: LocationEntry) {
        Logger.log("getDeviceLocation onUploadSkipped")
    }

    override fun onLocationAvailabilityChanged(locationAvailable: Boolean) {
        Logger.log("getDeviceLocation onLocationAvailabilityChanged")
    }

    override fun onMapReady(map: MapLibreMap) {
        map.setStyle(
            Style.Builder()
                .fromUri("https://maps.geo.${mainViewModel.identityPoolId.split(":")[0]}.amazonaws.com/maps/v0/maps/${mainViewModel.mapName}/style-descriptor"),
        ) {
            map.uiSettings.isAttributionEnabled = true
            map.uiSettings.isLogoEnabled = false
            map.uiSettings.attributionGravity = Gravity.BOTTOM or Gravity.END
            val initialPosition = LatLng(47.6160281982247, -122.32642111977668)
            map.cameraPosition = CameraPosition.Builder()
                .target(initialPosition)
                .zoom(14.0)
                .build()
        }
    }
}
