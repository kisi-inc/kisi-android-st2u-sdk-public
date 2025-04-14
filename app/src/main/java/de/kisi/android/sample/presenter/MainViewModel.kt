package de.kisi.android.sample.presenter

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.kisi.android.sample.BuildConfig
import de.kisi.android.service.ble.MotionSenseSettings
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // The context is injected directly into a ViewModel just to keep things simple
    // In the real world scenario you should have it abstracted away behind some interface
    // like DeviceServiceManager and have an instance of this manager injected into your ViewModel
    @ApplicationContext
    private val context: Context
): ViewModel() {

    private val bluetoothAdapter by lazy {
        val service = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        service.adapter
    }

    private val locationManager by lazy {
        context.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private val powerManager by lazy {
        context.getSystemService(POWER_SERVICE) as PowerManager
    }

    private val activityManager by lazy {
        context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private val supportsNearbyDevicePermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    private val supportsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private val refreshSubject = BehaviorSubject.createDefault(Unit)

    fun observe(): Observable<ScreenState> = refreshSubject.map {
        ScreenState(
            bluetoothPermissionState = if (!supportsNearbyDevicePermissions) {
                PermissionState.NOT_NEEDED
            } else if (isPermissionEnabled(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                PermissionState.GRANTED
            } else {
                PermissionState.DISABLED
            },
            isBluetoothOn = bluetoothAdapter.isEnabled,
            locationPermissionState = if (supportsNearbyDevicePermissions) {
                PermissionState.NOT_NEEDED
            } else if (isPermissionEnabled(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                PermissionState.GRANTED
            } else {
                PermissionState.DISABLED
            },
            isLocationAccessEnabled = LocationManagerCompat.isLocationEnabled(locationManager),
            notificationPermissionState = if (!supportsNotificationPermission) {
                PermissionState.NOT_NEEDED
            } else if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                PermissionState.GRANTED
            } else {
                PermissionState.DISABLED
            },
            isBatteryUsageGranted = run {
                val isIgnoringOptimizations = powerManager.isIgnoringBatteryOptimizations(
                    BuildConfig.APPLICATION_ID
                )

                val isBackgroundRestricted =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        // User could not enable background restrictions before P
                        false
                    } else {
                        activityManager.isBackgroundRestricted
                    }

                isIgnoringOptimizations && !isBackgroundRestricted
            },
            isMotionSenseEnabled = MotionSenseSettings.isEnabled,
        )
    }

    fun refresh() {
        refreshSubject.onNext(Unit)
    }

    private fun isPermissionEnabled(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    enum class PermissionState {
        NOT_NEEDED,
        DISABLED,
        GRANTED
    }

    data class ScreenState(
        val bluetoothPermissionState: PermissionState = PermissionState.DISABLED,
        val isBluetoothOn: Boolean = false,
        val locationPermissionState: PermissionState = PermissionState.DISABLED,
        val isLocationAccessEnabled: Boolean = false,
        val notificationPermissionState: PermissionState = PermissionState.DISABLED,
        val isBatteryUsageGranted: Boolean = false,
        val isMotionSenseEnabled: Boolean = false,
    )
}