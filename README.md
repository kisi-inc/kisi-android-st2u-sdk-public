## What "Tap-to-Unlock" is

"Tap-to-Unlock" (or "T2U") is Kisi's technology that makes it possible to access your premises by holding your smartphone up to the Kisi Reader Pro like you would a key card. The Reader will read your phone's Bluetooth (iPhone) or NFC (Android) signal to allow access.

Before you proceed with the integration of mobile SDK we recommend you to check the following:

### Kisi Readers and Controllers

This is the Kisi's hardware that you are going to use while working on the integration. You need to make sure your reader and controller are powered up, connected to the network, and properly set up. To do that, we recommend reading our [support articles](https://help.kisi.io/hc/en-us/categories/1500000269681-Installation) on hardware installation.

### Login objects

Login object is an entity returned by Kisi's API when the user signs in to Kisi. You need to obtain this object via Kisi's API, store it in the app's local cache and provide to our SDK as a part of its initialization code. To get more info on how Login objects are obtained we recommend to read [our article](https://docs.kisi.io/for_integration_partners/registration_and_authentication) on registration and authentication with Kisi.

## SDK Requirements

- Android 5.0 at minimum

## Integration

### Build Files

Get the latest AAR file from the [Releases](https://github.com/kisi-inc/kisi-android-st2u-sdk-public/releases) page and add it into your app module (`app/libs` by default), then open the `build.gradle` file of your app module and add dependencies needed by the SDK:

```gradle

dependencies {

    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])

    implementation "at.favre.lib:hkdf:1.1.0"
    implementation "io.reactivex.rxjava3:rxjava:3.0.0"
}
```

Sync the project with Gradle files.

### Card Emulation

"T2u" technology for Android is based on Android's [Host-based Card Emulation](https://developer.android.com/guide/topics/connectivity/nfc/hce) (or HCE), so the integration of this SDK consists of tying together the Android's entry point into host card emulation (called [HostApduService](https://developer.android.com/reference/android/nfc/cardemulation/HostApduService)) and Kisi's code.

We need to create a subclass of HostApduService, so let's take a look at how the sample implementation can look like:

```kotlin

class ScramTestService : HostApduService() {

    private lateinit var offlineMode: Scram3

    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        offlineMode = Scram3.with(
            clientId = 777,
            context = applicationContext,
            loginFetcher = { organizationId: Int? ->
                Maybe.just(
                    // ATTENTION - This is an example Login object that you will need to replace
                    // with a correct one obtained from Kisi's API. Read our integration docs
                    // mentioned above to get an idea of how to do that.
                    Login(
                        id = 42,
                        authenticationToken = "35B8ACFCF1F6AB6604CEB9F9157303A9",
                        phoneKey = "40CA258E7D5850C62068D70784B0DB7D",
                        onlineCertificate = "6D011D6C6F6D6E276BB6FEF7EFA5F87BBC3E7D9B945C786EAE5C086716F4B5EFF901D94A7DF90A98F1D9CEE6984F9588A8EF4CE59D8B20194A254BD7"
                    )
                )
            },
            onUnlockComplete = { }
        )
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        disposable = offlineMode
            .handle(commandApdu)
            .subscribeWith(
                object : DisposableSingleObserver<ByteArray>() {
                    override fun onSuccess(t: ByteArray) {
                        sendResponseApdu(t)
                    }

                    override fun onError(e: Throwable) {
                        sendResponseApdu(null)
                    }
                }
            )

        return null
    }

    override fun onDeactivated(reason: Int) {
        disposable?.dispose()
        disposable = null
    }
}
```

The key component that you're going to use is an implementation of `IOfflineMode` interface; at the time of this writing, there is only one implementation called `Scram3`. This class requires three parameters to perform its duties:

* An instance of Android's [Context](https://developer.android.com/reference/android/content/Context) class
* An integration partner specific id
* And a callback that returns an instance of Login class wrapped in RxJava's [Maybe](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Maybe.html).

An integration partner  specific id is a value that is used by Kisi to collect information on how different integrations perform and to offer help based on the integration partner specific logs. The information collected does not include any personal data. Please request a partner id by sending an email to sdks@kisi.io.

An instance of `Login` contains 4 properties, all of which you will get while signing the user in via Kisi's API:

* `id` corresponds to the `id` field of aforementioned request
* `authenticationToken` corresponds to the `authentication_token` field
* `phoneKey` corresponds to the `phone_key` field of the `scram_credentials` object
* `onlineCertificate` corresponds to the `online_certificate` field of the `scram_credentials` object

### Manifest

The last thing to do is to add the service to the app's manifest:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>

        <service
            android:name=".ScramTestService"
            android:exported="true"
            android:permission="android.permission.BIND_NFC_SERVICE">

            <intent-filter>
                <action android:name="android.nfc.cardemulation.action.HOST_APDU_SERVICE" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>

            <meta-data
                android:name="android.nfc.cardemulation.host_apdu_service"
                android:resource="@xml/hce_service" />
        </service>

    </application>
</manifest>
```

`hce_service` is an XML file shipped with Kisi's SDK. It defines the set of properties used by Android OS to determine which of the installed applications will handle an incoming NFC connection. You should not override this file because otherwise, Android OS won't pick your application as a handler of an incoming connection from Kisi's reader.

**Note: it's important that you don't start this service yourself during e.g. the start of your application**. It's Android OS's duty to start and stop this service when the smartphone is in the close vicinity of a Reader. Starting this service yourself will lead to unexpected results such as failed unlocks.

Build your app and tap the Kisi Reader you have with your smartphone. The green color of Reader's LED means the unlock attempt has succeeded. If you get a red color instead it means the unlock attempt has failed. We recommend to provide `onUnlockComplete` callback and log its argument in logcat to see the failure's reason.

### Optional Parameters of Scram3

`onUnlockComplete` is an optional parameter that you can provide to be notified when the unlock gets completed. `UnlockError` enumeration defines the list of existing errors:

```kotlin
enum class UnlockError {

    NONE,

    UNEXPECTED_COMMAND,
    LOCAL_LOGIN_MISSING,

    READER_PROOF_VALIDATION,

    CERTIFICATE_FETCH_DENIED,

    PHONE_LOCKED,
    PHONE_LOCK_MISSING
}
```

The `NONE` value corresponds to the successful completion of the unlock data exchange. It doesn't mean that the door will be unlocked - once the Reader gets the last piece of data, it sends an unlock request to the cloud, and this request may fail if, e.g., the user does not have access right to that particular door. So receiving the `NONE` value means that the smartphone and the Reader are done communicating with each other.

The `LOCAL_LOGIN_MISSING` value is returned when the `loginFetcher` method could not return a valid `Login` object.

`CERTIFICATE_FETCH_DENIED` value may be returned when the Reader is in the offline mode. In this case, the Reader needs the smartphone to request the temporary certificate from Kisi's API, and this request may fail due to the lack of access rights.

`PHONE_LOCKED` and `PHONE_LOCK_MISSING` correspond to Kisi's implementation of [two-factor authentication](https://www.getkisi.com/features/2fa) for unlocks. If the place administrator has enabled 2FA for premises, we're trying to answer two questions when unlock is happening:

1. Is the screen lock enabled on this device?
2. If it's enabled, is the screen currently locked?

If the answer to any of these two questions is negative, the notification asking the user to either enable the screen lock or unlock the device to prove their identity pops up on the screen. But what if the user has deliberately turned off notifications on their device? In this case, you can use the provided callback to be notified about an error and act accordingly later. E.g., you can show a popup in your application next time the user opens it to let them know that notifications need to be turned on.

`UNEXPECTED_COMMAND` and `READER_PROOF_VALIDATION` are the errors corresponding to the internal implementation of the unlock algorithm and are mainly exposed to be able to gather analytics.

### KisiBeaconTracker

If you want to be able to unlock Kisi-equipped doors from the application UI, you may encounter the situation where to unlock you need to know the lock's `proximity_proof` (see [here](https://api.kisi.io/docs/#/operations/unlockLock)).

To find one, you can use the class called `KisiBeaconTracker` shipped with this SDK. The instance of this class performs periodic Bluetooth Low Energy (or BLE) scans to find Kisi's Readers nearby. Each Reader is equipped with a BLE beacon emitting a signal that contains this proof. Whenever a tracker finds a beacon, it calculates its proximity proof and delivers it to you. Whenever the Reader is lost from the close vicinity (or its proximity proof is changed), you're notified as well.

First of all, add one more dependency to the `build.gradle` file of your application:

```gradle

dependencies {

    ...
    implementation 'aga.android:luch:0.3.1' 
    ...
}
```

Then add the required permissions to your app's manifest:

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

</manifest>
```

Then, create an instance of `KisiBeaconTracker` (we recommend doing it in the Activity hosting the list of locks, in which case you will be able to use Activity's lifecycle and start/stop scans accordingly):

```kotlin
private val beaconTracker = KisiBeaconTracker(
    context,
    onScanFailure = { e ->
        // Here you can get notifications about beacon scan failures
    },
    onBeaconsDetected = { beacons ->
        // TODO handle beacons
    }
)
```

Start a scanner when activity is resumed, stop it when it's paused:

```kotlin
override fun onResume() {
    super.onResume()
    beaconTracker.startRanging()
}

override fun onPause() {
    super.onPause()
    beaconTracker.stopRanging()
}
```

The `onBeaconsDetected` callback will be called each time there is a change in the nearby Kisi beacons. It will give you a `Set` of `KisiBeacon` instances:

```kotlin
data class KisiBeacon(
    val lockId: Long,
    val totp: Int = 0
)
```

To unlock a door with a Reader restriction, you need to find its beacon in the current set of nearby beacons and pass the `totp` parameter as a `proximity_proof`.

Please also note that Location permission is considered to be a runtime one in Android, which means the user might revoke it at any time (or even deny it). You need to make sure Location permission is allowed for foreground usage and Bluetooth/Location services are turned on to find beacons.
