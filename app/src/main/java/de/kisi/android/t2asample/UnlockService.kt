package de.kisi.android.t2asample

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import de.kisi.android.st2u.IOfflineMode
import de.kisi.android.st2u.Login
import de.kisi.android.st2u.Scram3
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver

class UnlockService : HostApduService() {

    private lateinit var offlineMode: IOfflineMode

    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        offlineMode = Scram3.with(
            clientId = 777, // Replace it with an integration partner id you received from us
            context = applicationContext,
            loginFetcher = {
                // In the release version of your app you'd probably sign in your user to Kisi API
                // at some point of your user flow, and store the data of the Login object in the
                // local cache. You then need to fetch the data from the local cache and provide
                // it here.
                Maybe.just(
                    Login(
                        id = 42,
                        secret = "35B8ACFCF1F6AB6604CEB9F9157303A9",
                        phoneKey = "40CA258E7D5850C62068D70784B0DB7D",
                        onlineCertificate = "6D011D6C6F6D6E276BB6FEF7EFA5F87BBC3E7D9B945C786EAE5C086716F4B5EFF901D94A7DF90A98F1D9CEE6984F9588A8EF4CE59D8B20194A254BD7"
                    )
                )
            },
            onUnlockComplete = {
                Log.d("UnlockService", "Unlock error: $it")
            }
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