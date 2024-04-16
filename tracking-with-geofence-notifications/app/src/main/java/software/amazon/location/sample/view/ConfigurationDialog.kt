package software.amazon.location.sample.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import software.amazon.location.sample.R
import software.amazon.location.sample.viewModel.MainViewModel

@Composable
fun ConfigurationDialog(
    setShowDialog: (Boolean) -> Unit,
    mainViewModel: MainViewModel,
    onSignInFromConfigurationClicked: () -> Unit
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.testTag("identity_pool_id"),
                        value = mainViewModel.identityPoolId,
                        onValueChange = {
                            mainViewModel.identityPoolId = it
                        },
                        label = { Text(stringResource(R.string.label_identity_pool_id)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mainViewModel.trackerName,
                        onValueChange = {
                            mainViewModel.trackerName = it
                        },
                        label = { Text(stringResource(R.string.label_tracker_name)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mainViewModel.mapName,
                        onValueChange = {
                            mainViewModel.mapName = it
                        },
                        label = { Text(stringResource(R.string.label_map_name)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            modifier = Modifier.testTag("btn_sign_in"),
                            onClick = onSignInFromConfigurationClicked,
                        ) {
                            Text(
                                stringResource(R.string.btn_sign_in),
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        if (mainViewModel.isLoading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConfigurationDialogPreview() {
    ConfigurationScreen(
        mainViewModel = MainViewModel(),
        context = LocalContext.current,
        mapReadyCallback = {  },
        onSignInClicked = {  }) {
    }
}