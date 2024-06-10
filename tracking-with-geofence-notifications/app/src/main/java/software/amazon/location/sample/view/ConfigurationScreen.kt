package software.amazon.location.sample.view

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import software.amazon.location.sample.R
import software.amazon.location.sample.utils.Constant.MAX_DISTANCE_FILTER_VALUE
import software.amazon.location.sample.utils.Constant.MAX_TIME_FILTER_VALUE
import software.amazon.location.sample.viewModel.MainViewModel
import software.amazon.location.tracking.filters.AccuracyLocationFilter
import software.amazon.location.tracking.filters.DistanceLocationFilter
import software.amazon.location.tracking.filters.TimeLocationFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConfigurationScreen(
    mainViewModel: MainViewModel,
    context: Context,
    mapReadyCallback: OnMapReadyCallback,
    onSignInClicked: () -> Unit,
    onSignInFromConfigurationClicked: () -> Unit
) {
    var autoIncrementJob: Job? = null
    var autoDecrementJob: Job? = null
    val coroutineScope = MainScope()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Auth section
            Text(
                text = stringResource(R.string.label_auth_section),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onSignInClicked
                ) {
                    Text(
                        if (mainViewModel.authenticated) {
                            stringResource(R.string.btn_sign_out)
                        } else {
                            stringResource(R.string.btn_sign_in)
                        },
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (mainViewModel.isLoading) {
                    CircularProgressIndicator()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Config section
            Text(
                text = stringResource(R.string.label_config_section),
                style = MaterialTheme.typography.headlineLarge
            )

            // Disabled section if not logged in
            if (mainViewModel.authenticated) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(modifier = Modifier.testTag("text_time_filter"),text = stringResource(R.string.label_time_filter))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        modifier = Modifier.testTag("switch_time_filter"),
                        checked = mainViewModel.timeFilterEnabled,
                        onCheckedChange = {
                            mainViewModel.timeFilterEnabled = it
                            if (it) {
                                mainViewModel.locationTracker?.enableFilter(
                                    TimeLocationFilter(
                                        mainViewModel.timeInterval * 1000
                                    )
                                )
                            } else {
                                mainViewModel.locationTracker?.disableFilter(TimeLocationFilter())
                            }
                        },
                    )
                }

                if (mainViewModel.timeFilterEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(modifier = Modifier.testTag("time_filter_value"), text = "Filter value: ${mainViewModel.timeInterval} ${context.getString(R.string.label_second)}")
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                                .background(Color.White, CircleShape),
                        ) {
                            IconButton(
                                onClick = {
                                    if (mainViewModel.timeInterval < MAX_TIME_FILTER_VALUE) {
                                        mainViewModel.timeInterval += 1
                                        mainViewModel.setTimeFilterData()
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("icon_time_plus")
                                    .pointerInteropFilter { event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                mainViewModel.isIncrementing = true
                                                autoIncrementJob = coroutineScope.launch {
                                                    while (mainViewModel.isIncrementing && mainViewModel.timeInterval < MAX_TIME_FILTER_VALUE) {
                                                        mainViewModel.timeInterval += 1
                                                        delay(200)
                                                    }
                                                }
                                            }

                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                mainViewModel.setTimeFilterData()
                                                mainViewModel.isIncrementing = false
                                                autoIncrementJob?.cancel()
                                            }
                                        }
                                        true
                                    }
                                    .then(
                                        Modifier.semantics { contentDescription = "icon_time_plus" }
                                    ),
                            ) {
                                Text("+", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                                .background(Color.White, CircleShape),
                        ) {
                            IconButton(
                                onClick = {
                                    if (mainViewModel.timeInterval > 0) mainViewModel.timeInterval -= 1
                                    mainViewModel.setTimeFilterData()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("icon_time_minus")
                                    .pointerInteropFilter { event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                mainViewModel.isDecrementing = true
                                                autoDecrementJob = coroutineScope.launch {
                                                    while (mainViewModel.isDecrementing && mainViewModel.timeInterval > 0) {
                                                        mainViewModel.timeInterval -= 1
                                                        delay(200)
                                                    }
                                                }
                                            }

                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                mainViewModel.setTimeFilterData()
                                                mainViewModel.isDecrementing = false
                                                autoDecrementJob?.cancel()
                                            }
                                        }
                                        true
                                    }.then(
                                        Modifier.semantics { contentDescription = "icon_time_minus" }
                                    ),
                            ) {
                                Text("-", fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.label_distance_filter))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        modifier = Modifier.testTag("switch_distance_filter"),
                        checked = mainViewModel.distanceFilterEnabled,
                        onCheckedChange = {
                            mainViewModel.distanceFilterEnabled = it
                            if (it) {
                                mainViewModel.locationTracker?.enableFilter(
                                    DistanceLocationFilter(
                                        mainViewModel.distanceThreshold,
                                    ),
                                )
                            } else {
                                mainViewModel.locationTracker?.disableFilter(DistanceLocationFilter())
                            }
                        },
                    )
                }

                if (mainViewModel.distanceFilterEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(modifier = Modifier.testTag("distance_filter_value"), text = "Filter value: ${mainViewModel.distanceThreshold} ${context.getString(R.string.label_meter)}")
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                                .background(Color.White, CircleShape),
                        ) {
                            IconButton(
                                onClick = {
                                    if (mainViewModel.distanceThreshold < MAX_DISTANCE_FILTER_VALUE) {
                                        mainViewModel.distanceThreshold += 1
                                        mainViewModel.setDistanceFilterData()
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("icon_distance_plus")
                                    .pointerInteropFilter { event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                mainViewModel.isIncrementing = true
                                                autoIncrementJob = coroutineScope.launch {
                                                    while (mainViewModel.isIncrementing && mainViewModel.distanceThreshold < MAX_DISTANCE_FILTER_VALUE) {
                                                        mainViewModel.distanceThreshold += 1
                                                        delay(200)
                                                    }
                                                }
                                            }

                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                mainViewModel.setDistanceFilterData()
                                                mainViewModel.isIncrementing = false
                                                autoIncrementJob?.cancel()
                                            }
                                        }
                                        true
                                    }.then(
                                        Modifier.semantics { contentDescription = "icon_distance_plus" }
                                    ),
                            ) {
                                Text("+", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                                .background(Color.White, CircleShape),
                        ) {
                            IconButton(
                                onClick = {
                                    if (mainViewModel.distanceThreshold > 0) mainViewModel.distanceThreshold -= 1
                                    mainViewModel.setDistanceFilterData()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("icon_distance_minus")
                                    .pointerInteropFilter { event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                mainViewModel.isDecrementing = true
                                                autoDecrementJob = coroutineScope.launch {
                                                    while (mainViewModel.isDecrementing && mainViewModel.distanceThreshold > 0) {
                                                        mainViewModel.distanceThreshold -= 1
                                                        delay(200)
                                                    }
                                                }
                                            }

                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                mainViewModel.setDistanceFilterData()
                                                mainViewModel.isDecrementing = false
                                                autoDecrementJob?.cancel()
                                            }
                                        }
                                        true
                                    }.then(
                                        Modifier.semantics { contentDescription = "icon_distance_minus" }
                                    ),
                            ) {
                                Text("-", fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.label_accuracy_filter))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        modifier = Modifier.testTag("switch_accuracy_filter"),
                        checked = mainViewModel.accuracyFilterEnabled,
                        onCheckedChange = {
                            mainViewModel.accuracyFilterEnabled = it
                            if (it) {
                                mainViewModel.locationTracker?.enableFilter(AccuracyLocationFilter())
                            } else {
                                mainViewModel.locationTracker?.disableFilter(AccuracyLocationFilter())
                            }
                        },
                    )
                }
                if (mainViewModel.accuracyFilterEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Accuracy measured: ${mainViewModel.lastAccuracyMeasured}")
                }
            } else {
                Text(
                    stringResource(R.string.label_log_in_to_access_configuration_settings),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (mainViewModel.authenticated) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White),
                ) {
                    MapView(mapReadyCallback)
                }
            }
        }
        if (mainViewModel.isConfigDialogVisible) {
            ConfigurationDialog(setShowDialog = {
                mainViewModel.isConfigDialogVisible = it
            }, mainViewModel, onSignInFromConfigurationClicked = onSignInFromConfigurationClicked)
        }
    }
}

@Composable
fun MapView(mapReadyCallback: OnMapReadyCallback) {
    AndroidView(
        modifier = Modifier.testTag("map_view"),
        factory = { context ->
            val mapView = MapView(context)
            mapView.onCreate(null)
            mapView.getMapAsync(mapReadyCallback)
            mapView
        },
    )
}