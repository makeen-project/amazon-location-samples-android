package com.amazon.androidquickstartapp.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amazon.androidquickstartapp.R
import com.amazon.androidquickstartapp.ui.viewModel.MainViewModel
import org.maplibre.android.maps.OnMapReadyCallback

@Composable
fun MapLoadScreen(
    mainViewModel: MainViewModel,
    mapReadyCallback: OnMapReadyCallback,
    onLocateMeClick: () -> Unit,
    onStartStopTrackingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        MapView(mapReadyCallback)
        Box(
            modifier = Modifier
                .align(Alignment.Center),
        ) {
            Image(
                painter = painterResource(id = R.drawable.red_marker),
                contentDescription = "marker",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                FloatingActionButton(
                    onClick = onLocateMeClick,
                    containerColor = Color.White,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_my_location),
                        contentDescription = "my_location",
                        colorFilter = ColorFilter.tint(if (mainViewModel.isFollowingLocationMarker) Color(0xFF2196F3) else Color.Gray)
                    )
                }
            }
            Spacer(modifier = Modifier.height(180.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                Text(
                    text = mainViewModel.label,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                        .testTag("label")
                        .semantics {
                            contentDescription = "label"
                        },
                    fontSize = 14.sp,
                    maxLines = 3
                )
            }
            Spacer(modifier = Modifier.height(70.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Button(
                onClick = onStartStopTrackingClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (mainViewModel.isLocationTrackingForegroundActive) stringResource(R.string.stop_tracking) else stringResource(
                        R.string.start_tracking
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Image(
                    painter = painterResource(id = if (mainViewModel.isLocationTrackingForegroundActive) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (mainViewModel.isLocationTrackingForegroundActive) "stop_tracking" else "start_tracking"
                )
            }
        }
    }
}

@Composable
fun MapView(mapReadyCallback: OnMapReadyCallback) {
    AndroidView(
        modifier = Modifier
            .testTag("map_view")
            .semantics {
                contentDescription = "map_view"
            },
        factory = { context ->
            val mapView = org.maplibre.android.maps.MapView(context)
            mapView.onCreate(null)
            mapView.getMapAsync(mapReadyCallback)
            mapView
        },
    )
}