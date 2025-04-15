package de.kisi.android.sample.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import de.kisi.android.sample.BuildConfig
import de.kisi.android.sample.presenter.MainViewModel
import de.kisi.android.service.ble.MotionSenseSettings
import de.kisi.android.service.ble.start.MotionSenseStarter

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private companion object {

        const val PERMISSION_REQUEST_KEY = 101

        val BLUETOOTH_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val uiState by viewModel
                .observe()
                .subscribeAsState(MainViewModel.ScreenState())

            MainScreen(
                uiState = uiState,
                onLocationPermissionGrantRequested = {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                        PERMISSION_REQUEST_KEY
                    )
                },
                onLocationEnableRequested = {
                    startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                },
                onBluetoothPermissionGrantRequested = {
                    ActivityCompat.requestPermissions(
                        this,
                        BLUETOOTH_PERMISSIONS,
                        PERMISSION_REQUEST_KEY
                    )
                },
                onBluetoothEnableRequested = {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } else {
                        // We can't get to this state since the button is blocked until permission
                        // gets granted
                    }
                },
                onNotificationsPermissionGrantRequested = {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_KEY
                    )
                },
                onBatteryUsageGrantRequested = {
                    startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null))
                    )
                },
                onMotionSenseStateChanged = { isChecked ->
                    MotionSenseSettings.isEnabled = isChecked

                    viewModel.refresh()

                    if (isChecked) {
                        MotionSenseStarter.start()
                    } else {
                        MotionSenseStarter.stop()
                    }
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        MotionSenseStarter.start()
        viewModel.refresh()
    }
}