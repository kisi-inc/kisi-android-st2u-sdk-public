The top-level README file contains in-depth instructions on what this SDK can and can't do, as well as how to use it.

This README file contains a short list of things to look for in the sample app.

### SDK Configuration

`SecureUnlockSampleApplication` is where the SDK gets initialized. Create your own subclass of Android's `Application` class and call the `SecureUnlockConfiguration#init` method.

You need to provide the `Context`, the client ID you received from us, and the `Login` object. You can read more about how this `Login` object gets created in the top-level README.

That’s actually all you need to do to add support for NFC-powered unlocks. The Android OS does most of the heavy lifting itself, spawning the unlock services when needed and stopping them when the NFC connection is lost.

In the case of Motion Sense, you’ll need to do a bit more. See the top-level README file for precise instructions.

### Motion Sense Notification's App Icon

Motion Sense runs through a foreground service due to platform restrictions. A foreground service requires a persistent notification, and that notification should display an icon that corresponds to your app.

* If you want to override the default icon provided by the SDK with one that matches your brand, add a drawable resource called `ic_msense_notification` to your app’s resource set (see the `res/drawable` directory in this sample).
* If you want to change the background color of this icon, add a color resource called `color_msense_notification` to your app’s resource set.