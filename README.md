# Kisi Tap to Unlock SDK for Android

## 1. Who is this SDK for?

This SDK is for all Kisi partners and customers who want to integrate seamless access control into their own mobile app. Using the SDK, they can provide Kisi-powered mobile credentials for their end users, such as:
* **Tap to unlock**: allowing users to open a door only by holding their mobile device up to a Kisi Reader
* **Tap in-app**: allowing users to unlock doors by tapping within their app.

## 2. What does this SDK include?
* Tap to unlock functionality (also referred to as ‘Tap to access’)
* Kisi's BLE beacon scanner functionality. This is needed to satisfy the requirements of [reader restrictions](https://docs.kisi.io/concepts/restrictions#kisi-reader-restriction) for in-app unlocks. The reader restriction feature ensures that users may only unlock when standing in front of a door.

## 3. What this SDK doesn’t include
* Signing in ([described here](https://docs.kisi.io/api/how_to_guides/manage_users/#log-in-on-behalf-of-managed-users))
* In-app unlock calls
* UI

## 4. Update the SDK
It’s important that you regularly update the SDK. Versions earlier than 0.6 don’t support [offline cache](https://docs.kisi.io/concepts/offline_support#offline-cache-on-the-reader) and [BLE beacon scanner](https://docs.kisi.io/concepts/restrictions#kisi-reader-restriction). This means, you won't be able to use these features unless you update the SDK.

For the list of changes, please see the [release change log](https://github.com/kisi-inc/kisi-android-st2u-sdk-public/releases). Get further help under the following links:

* If it turns out that you may have found a bug, please [open an issue](https://github.com/kisi-inc/kisi-android-st2u-sdk-public/issues)
* For questions related to the Kisi API, check out our [API integration guides](https://docs.kisi.io/api/) and [API reference](https://api.getkisi.com/docs#/)
* To understand basic Kisi concepts, visit our [product documentation](https://docs.kisi.io/)

## 5. Get started with Tap to unlock
Tap to unlock allows users to unlock doors by tapping their Android mobile device against the Kisi Reader, without having to actively use your app. The communication is based on NFC technology, transmitting the information to the controller and to the cloud. Communication with the controller can also be through a local network. If the local network is offline, the phone will request offline credentials from the cloud. In this case, the reader will talk to the controller without any intermediaries. (Note: We only support [offline cache](https://docs.kisi.io/concepts/offline_support) on versions later than 0.6).

### 5.1 Prerequisites

* **Android 5.0 at minimum**
* **Kisi organization administrator rights**
* **Kisi hardware setup**: To use the SDK, you must have a Kisi controller and reader set up, powered up and connected to the network. If you don’t have this yet, please follow our articles on [how to install the Kisi hardware](https://docs.kisi.io/get_started/install_your_kisi_hardware/).
* **A Kisi partner ID**: This is used by Kisi to collect information on how different integrations perform and to offer help based on the integration partner specific logs. The information collected does not include any personal data. You can request a partner id by sending an email to sdks@kisi.io.

### 5.2 Create an account and sign in

If you don’t have one yet, [create a Kisi account and sign in](https://docs.kisi.io/api/get_started/create_account_and_sign_in). Make sure you have organization administrator rights, since this will be needed in the following steps.

### 5.3 Create an admin login

Next, [create an admin login](https://docs.kisi.io/api/get_started/create_login) to obtain an API key. This will allow you to make requests to the Kisi API without having to enter your user credentials each time.

## 6. Log in on behalf of your users

Since your users won’t be able to log in to Kisi themselves, your app will need to do this automatically, on their behalf. To achieve this, you need to add a Kisi API call into your sign-in flow, as [shown in our API guide](https://docs.kisi.io/api/how_to_guides/manage_users/#log-in-on-behalf-of-managed-users).

You need to store this login object in your app's local cache and provide it to our SDK as a part of its initialization code. This login can then be used subsequently to make requests to the Kisi API on behalf of the user.

## 7. Integrate the SDK

### 7.1 Get the necessary files

As the first step to integrate the SDK, you need to get your build files ready.

1. Go to the [Releases](https://github.com/kisi-inc/kisi-android-st2u-sdk-public/releases) page
2. Get the latest AAR file
3. Add it into your app module (`app/libs` by default)
4. Open the build.gradle file of your app module
5. Add the dependencies needed by the SDK, as shown below:

```gradle

dependencies {

    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])

    implementation "at.favre.lib:hkdf:1.1.0"
    implementation "io.reactivex.rxjava3:rxjava:3.0.0"
}
```

6. Sync the project with Gradle files.

### 7.2 Create a subclass of `HostApduService`
The Tap to Unlock technology for Android is based on Android's [Host-based Card Emulation](https://developer.android.com/guide/topics/connectivity/nfc/hce) (or HCE). This means, the integration of this SDK consists of tying together the Android's entry point into host card emulation (called [HostApduService](https://developer.android.com/reference/android/nfc/cardemulation/HostApduService)) and Kisi's code.

In this step, you create a subclass of `HostApduService`.

The key component that you're going to use is an implementation of `IOfflineMode` interface; at the time of this writing, there is only one implementation called `Scram3`. This class requires three parameters to perform its duties:

* An instance of Android's `Context` class
* You integration partner specific id
* And a callback that returns an instance of `Login` class wrapped in RxJava's [Maybe](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Maybe.html)

See below how the sample implementation can look like.

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

An instance of `Login` contains four properties, all of which you will get while [signing the user in via Kisi's API](https://docs.google.com/document/d/1Rv_13f9uO2DInWOpOXIYQKj3NIZ-jdR1hi_MJLOaTgg/edit#heading=h.mu1n9ubjw7fj):

* `id` corresponds to the `id` field of aforementioned request
* `secret` corresponds to the `secret` field
* `phoneKey` corresponds to the `phone_key` field of the `scram_credentials` object
* `onlineCertificate` corresponds to the `online_certificate` field of the `scram_credentials` object

### 7.3 Add the service to the app's manifest
As a last step, you need to add the service to the app's manifest, as shown below.

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

`hce_service` is an XML file shipped with Kisi's SDK. It defines the set of properties used by Android OS to determine which of the installed applications will handle an incoming NFC connection.

**Important notes:**

1. You should not override the `hce_service` file, otherwise the Android OS won't pick your application as a handler of an incoming connection from Kisi's reader.
2. Don't start this service yourself during e.g. the start of your application, as starting this service yourself will lead to unexpected results such as failed unlocks. It's Android OS's duty to start and stop this service when the smartphone is in the close vicinity of a reader.

### 7.4 Test your app

Once you’ve built your app, tap your Android device to your Kisi reader.

* If the reader LED is blinking up green, it means that the unlock was successful.
* If the reader LED is blinking up red, it means the unlock attempt has failed.

Tap to unlock does not require any specific Android permissions. All you need from your users is to make sure they keep NFC on.

**Tip**: As an optional parameter, we recommend to provide the `onUnlockComplete` callback and log its argument in logcat to see the failure's reason (as shown in the example under 5.2).

## 8. List of Errors

The `UnlockError` enumeration defines the list of existing errors:

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

* `NONE` -- the value corresponds to the successful completion of the unlock data exchange. However, it doesn't mean that the door will be unlocked. Once the Reader gets the last piece of data, it sends an unlock request to the cloud, and this request may fail if, e.g., the user does not have access right to that particular door. So receiving the `NONE` value simply means that the communication between the Android device and the Kisi reader was successful.
* `LOCAL_LOGIN_MISSING` - this value is returned when the `loginFetcher` method could not return a valid `Login` object.
* `CERTIFICATE_FETCH_DENIED` - this value may be returned when the Reader is in the offline mode. In this case, the Reader needs the smartphone to request the temporary certificate from Kisi's API, and this request may fail due to the lack of access rights.
* `PHONE_LOCKED` and `PHONE_LOCK_MISSING` correspond to Kisi's implementation of [two-factor authentication](https://www.getkisi.com/features/2fa) for unlocks. If the place administrator has enabled 2FA for premises, we're trying to answer two questions when unlock is happening:

1. Is the screen lock enabled on this device?
2. If it's enabled, is the screen currently locked?

If the answer to any of these two questions is negative, a notification pops up on the screen, asking the user to either enable the screen lock or unlock the device to prove their identity.

If the user has deliberately turned off notifications on their device, you can use the provided callback to be notified about an error and act accordingly later. E.g., you can show a popup in your application next time the user opens it to let them know that notifications need to be turned on.

* `UNEXPECTED_COMMAND` and `READER_PROOF_VALIDATION` - these are errors corresponding to the internal implementation of the unlock algorithm and are mainly exposed to be able to gather analytics.

## 9. Satisfy reader restrictions in in-app unlocks
Tap in-app allows users to unlock doors from within your Android app. The tap sends the unlock request directly to the cloud, then to the controller, which fires the relay opening the door.

**Note**: If you have implemented the Tap to unlock scenario discussed above, you can skip the bullets below and jump directly to 9.1. If not, please follow the steps below.

**Prerequisites**
* Android 5.0 at minimum
* Kisi organization administrator rights
* Kisi hardware setup: To use the SDK, you must have a Kisi controller and reader set up, powered up and connected to the network. To achieve this, follow our articles on [how to install the Kisi hardware](https://docs.kisi.io/get_started/install_your_kisi_hardware/).

#### Create an account and sign in as described under 5.2
#### Log in as described under 5.3
#### Log in on behalf of your users as described under 6

### 9.1 Get the necessary files

1. Get your build files ready as described under 7.1.
2. Add a further dependency, as shown below:

```gradle

dependencies {

    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])

    implementation "at.favre.lib:hkdf:1.1.0"
    implementation "io.reactivex.rxjava3:rxjava:3.0.0"
    implementation 'aga.android:luch:0.3.1'
}
```

3. Sync the project with Gradle files.

### 9.2 Add the required permissions

The Tap in-app scenario requires a few permissions, so you need to make sure to add these to your app's manifest, as shown below.

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

</manifest>
```

* `ACCESS_FINE_LOCATION` is needed to be able to find BLE beacons. This is a restriction imposed by Android OS because, in theory, beacons can be used to derive physical location of a device. Note that this permission is considered to be a runtime/dangerous one in Android, which means the user might revoke it at any time (or even deny it). Therefore, you need to make sure Location permission is allowed for foreground usage and Bluetooth/Location services are turned on to find beacons.
* `BLUETOOTH` and `BLUETOOTH_ADMIN` permissions are needed to be able to access Bluetooth APIs of Android OS. Both are considered to be normal, i.e. these are granted upon app installation.

### 9.3 Create a Kisi beacon tracker

To make sure that users may only unlock when standing in front of the door, a [Kisi Reader restriction](https://docs.kisi.io/concepts/restrictions#kisi-reader-restriction) might be applied to the door. Therefore, while unlocking, your app will need to prove that the user is within the allowed distance from the reader.

To achieve this, you will have to obtain the door's `proximity_proof` (see [here](https://api.kisi.io/docs/#/operations/unlockLock)).

To find the `proximity_proof`, you can use the class called `KisiBeaconTracker` shipped with this SDK. The instance of this class performs periodic Bluetooth Low Energy (or BLE) scans to find Kisi readers nearby. Each reader is equipped with a BLE beacon emitting a signal that contains this proof. Whenever a tracker finds a beacon, it calculates its proximity proof and delivers it to you. Whenever the reader is lost from the close vicinity (or its proximity proof is changed), you're notified as well.

1. Create an instance of `KisiBeaconTracker` as shown below

**Tip**: it’s recommended that you do in the `Activity` hosting the list of locks, so that you will be able to use `Activity`'s lifecycle and start/stop scans accordingly):

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

2. Start a scanner when activity is resumed, stop it when it's paused, as shown below:

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

### 9.4 Pass the proximity proof to the Kisi API

Once you’ve found the totp parameter (shown above), you need to pass it into the Kisi API. You can do it by sending a POST request to the Kisi API’s Unlock lock endpoint, and entering the totp parameter as the proximity_proof, as shown below:

```curl
curl --request POST \
--url https://api.kisi.io/locks \
--header 'Authorization: KISI-LOGIN <API_KEY>' \
--header 'Content-Type: application/json' \
--data '{
"lock": {
"proximity_proof": "string"
}
}'
```