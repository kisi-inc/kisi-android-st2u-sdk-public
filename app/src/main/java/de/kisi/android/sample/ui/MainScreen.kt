package de.kisi.android.sample.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.kisi.android.sample.R
import de.kisi.android.sample.presenter.MainViewModel.PermissionState
import de.kisi.android.sample.presenter.MainViewModel.ScreenState

@Composable
fun MainScreen(
    uiState: ScreenState,
    onBluetoothPermissionGrantRequested: () -> Unit = { },
    onBluetoothEnableRequested: () -> Unit = { },
    onLocationPermissionGrantRequested: () -> Unit = { },
    onLocationEnableRequested: () -> Unit = { },
    onNotificationsPermissionGrantRequested: () -> Unit = { },
    onBatteryUsageGrantRequested: () -> Unit = { },
    onMotionSenseStateChanged: (Boolean) -> Unit = { },
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp)
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Header(
            title = stringResource(R.string.nfc_unlock_state)
        )

        Text(
            text = stringResource(R.string.info_nfc),
            style = MaterialTheme.typography.bodyLarge
        )

        Header(
            title = stringResource(R.string.motion_sense_state),
        )

        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(R.string.info_motion_sense),
            style = MaterialTheme.typography.bodyLarge
        )

        val capabilityModifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)

        if (uiState.bluetoothPermissionState != PermissionState.NOT_NEEDED) {
            MotionSenseCapabilityState(
                modifier = capabilityModifier,
                title = stringResource(R.string.bluetooth_permissions),
                isEnabled = uiState.bluetoothPermissionState == PermissionState.GRANTED,
                onClick = onBluetoothPermissionGrantRequested
            )

            MotionSenseCapabilityState(
                modifier = capabilityModifier,
                title = stringResource(R.string.bluetooth_access),
                isEnabled = uiState.isBluetoothOn,
                // We can't request Bluetooth adapter to be turned on unless user has granted
                // us the runtime Bluetooth permissions. So the button will be blocked until
                // permission is granted
                isBlocked = uiState.bluetoothPermissionState != PermissionState.GRANTED,
                onClick = onBluetoothEnableRequested
            )
        }

        if (uiState.locationPermissionState != PermissionState.NOT_NEEDED) {
            MotionSenseCapabilityState(
                modifier = capabilityModifier,
                title = stringResource(R.string.location_permissions),
                isEnabled = uiState.locationPermissionState == PermissionState.GRANTED,
                onClick = onLocationPermissionGrantRequested
            )

            MotionSenseCapabilityState(
                modifier = capabilityModifier,
                title = stringResource(R.string.location_access),
                isEnabled = uiState.isLocationAccessEnabled,
                onClick = onLocationEnableRequested
            )
        }

        MotionSenseCapabilityState(
            modifier = capabilityModifier,
            title = stringResource(R.string.notifications_permission),
            isEnabled = uiState.notificationPermissionState == PermissionState.GRANTED,
            onClick = onNotificationsPermissionGrantRequested
        )

        MotionSenseCapabilityState(
            modifier = capabilityModifier,
            title = stringResource(R.string.battery_access),
            isEnabled = uiState.isBatteryUsageGranted,
            onClick = onBatteryUsageGrantRequested
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.motion_sense_state),
                    style = MaterialTheme.typography.bodyLarge
                )

                Switch(
                    checked = uiState.isMotionSenseEnabled,
                    onCheckedChange = onMotionSenseStateChanged
                )
            }
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        text = title,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun MotionSenseCapabilityState(
    modifier: Modifier = Modifier,
    title: String,
    isEnabled: Boolean,
    isBlocked: Boolean = false,
    onClick: () -> Unit = { },
) {

    AnimatedContent(isEnabled) { isEnabled ->
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(if (isEnabled) R.string.on else R.string.off),
                    color = if (isEnabled) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.weight(1.0f))

                Button(onClick = onClick, enabled = isBlocked || !isEnabled) {
                    Text(
                        stringResource(
                            if (isEnabled) R.string.enabled else R.string.enable
                        )
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun MainScreenPreAndroid11Preview() {
    MainScreen(
        ScreenState(
            bluetoothPermissionState = PermissionState.NOT_NEEDED,
            isBluetoothOn = true,
            locationPermissionState = PermissionState.DISABLED,
            isLocationAccessEnabled = true,
            notificationPermissionState = PermissionState.NOT_NEEDED,
            isBatteryUsageGranted = false,
            isMotionSenseEnabled = false,
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun MainScreenPostAndroid11Preview() {
    MainScreen(
        ScreenState(
            bluetoothPermissionState = PermissionState.GRANTED,
            isBluetoothOn = false,
            locationPermissionState = PermissionState.NOT_NEEDED,
            isLocationAccessEnabled = false,
            notificationPermissionState = PermissionState.GRANTED,
            isBatteryUsageGranted = true,
            isMotionSenseEnabled = false,
        )
    )
}

@Composable
@Preview(showBackground = true)
private fun MainScreenPostMotionSenseEnabledPreview() {
    MainScreen(
        ScreenState(
            bluetoothPermissionState = PermissionState.GRANTED,
            isBluetoothOn = true,
            locationPermissionState = PermissionState.NOT_NEEDED,
            isLocationAccessEnabled = false,
            notificationPermissionState = PermissionState.GRANTED,
            isBatteryUsageGranted = true,
            isMotionSenseEnabled = true,
        )
    )
}