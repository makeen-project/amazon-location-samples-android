package software.amazon.location.sample.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import software.amazon.location.sample.R
import software.amazon.location.sample.viewModel.MainViewModel

@Composable
fun TrackingScreen(
    mainViewModel: MainViewModel,
    onStartTrackingForeground: () -> Unit,
    onStartTrackingBackground: () -> Unit,
    onStartTrackingBatteryOptimization: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            enabled = mainViewModel.authenticated,
            onClick = onStartTrackingForeground,
            modifier = Modifier.fillMaxWidth().testTag("btn_tracking_foreground"),
        ) {
            Text(
                if (mainViewModel.isLocationTrackingForegroundActive) {
                    stringResource(R.string.btn_stop_tracking)
                } else {
                    stringResource(R.string.btn_start_tracking)
                },
            )
        }

        Button(
            enabled = mainViewModel.authenticated,
            onClick = onStartTrackingBackground,
            modifier = Modifier.fillMaxWidth().testTag("btn_tracking_background"),
        ) {
            Text(
                if (mainViewModel.isLocationTrackingBackgroundActive) {
                    stringResource(R.string.stop_tracking_in_background)
                } else {
                    stringResource(R.string.start_tracking_in_background)
                },
            )
        }

        Button(
            enabled = mainViewModel.authenticated,
            onClick = onStartTrackingBatteryOptimization,
            modifier = Modifier.fillMaxWidth().testTag("btn_tracking_battery_saver"),
        ) {
            Text(
                if (mainViewModel.isLocationTrackingBatteryOptimizeActive) {
                    stringResource(R.string.btn_battery_optimization_stop)
                } else {
                    stringResource(R.string.btn_battery_optimization_start)
                },
            )
        }
    }
}