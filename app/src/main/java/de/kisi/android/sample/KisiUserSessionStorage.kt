package de.kisi.android.sample

import de.kisi.android.st2u.Login
import io.reactivex.rxjava3.core.Maybe

/**
 * This object provides access to a Login object you obtained from Kisi API. The usual way to go
 * about it is to create a Login for your user through Kisi API, and then store it in the local
 * cache (SharedPreferences, SQLite, etc.). This login is needed to communicate with Kisi's
 * readers to perform an unlock. In order to run this sample app and perform an unlock, create
 * a Login object and replace id, secret, phoneKey and onlineCertificate you see below with the
 * data from your Login.
 *
 * Logins are usually created once and reused until the user logs out. You don't need to create
 * a new Login every time you want to unlock.
 */
object KisiUserSessionStorage {

    fun getUserSession(): Maybe<Login> {
        return Maybe.just(
            Login(
                id = 42,
                secret = "35B8ACFCF1F6AB6604CEB9F9157303A9",
                phoneKey = "40CA258E7D5850C62068D70784B0DB7D",
                onlineCertificate = "6D011D6C6F6D6E276BB6FEF7EFA5F87BBC3E7D9B945C786EAE5C086716F4B5EFF901D94A7DF90A98F1D9CEE6984F9588A8EF4CE59D8B20194A254BD7"
            )
        )
    }
}