package session

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

@SuppressLint("HardwareIds")
object Session {

    var peerId: String? = null

    fun getPeerId(context: Context): String {
        peerId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        return peerId!!
    }
}