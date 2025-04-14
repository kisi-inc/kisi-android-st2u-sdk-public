package de.kisi.android.sample

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import de.kisi.android.SecureUnlockConfiguration
import de.kisi.android.SecureUnlockLogger
import de.kisi.android.service.ble.start.MotionSenseStarter

/**
 * In the updated version of the Secure Unlock SDK, you no longer need to define unlock services
 * yourself. The only thing you need to do is to initialize the SDK in your Application class,
 * providing the client ID and a callback to fetch the Login object from your local cache.
 *
 * Please remove any definitions of the SecureUnlockService class from your code if you're upgrading
 * from one of the old versions of our SDK (older than 0.11).
 */
@HiltAndroidApp
class SecureUnlockSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SecureUnlockConfiguration.init(
            // Replace it with an integration partner id you received from us
            clientId = 777,
            context = applicationContext,
            fetchLoginCallback = {
                KisiUserSessionStorage.getUserSession()
            },
            onUnlockCompleteCallback = { unlockSource, unlockResult ->
                Log.d(
                    "SecureUnlock",
                    "an unlock has just happened through $unlockSource, the result is $unlockResult"
                )
            }
        )

        SecureUnlockLogger.setOutput(
            object : SecureUnlockLogger.Logger {
                override fun log(
                    log: String,
                    priority: SecureUnlockLogger.Logger.Priority,
                ) {
                    when (priority) {
                        SecureUnlockLogger.Logger.Priority.DEBUG -> {
                            Log.d("SecureUnlock", log)
                        }
                        SecureUnlockLogger.Logger.Priority.INFO -> {
                            Log.i("SecureUnlock", log)
                        }
                    }
                }

                override fun log(throwable: Throwable) = Unit
            }
        )

        // This will only start Motion Sense if all required permissions have already been granted.
        // Therefore, we recommend starting it in two places:
        // 1. In the onCreate method of your Application class.
        // 2. In the part of your UI flow where you confirm that Bluetooth, Location, and Battery usage
        //    permissions have been granted.
        // See also: MainActivity
        MotionSenseStarter.start()
    }
}