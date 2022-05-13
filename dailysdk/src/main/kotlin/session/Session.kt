package session

import android.provider.Settings
import org.webrtc.ContextUtils.getApplicationContext

class Session {

    //    val uuid = UUID.randomUUID().toString()
    val uuid = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
    val peerId = uuid
    var rtcpCapabilitiesJson: String? = null
    var sendTransportJson: String? = null
    var sendTransportId: String? = null
    var recvTransportJson: String? = null
    var recvTransportId: String? = null

    companion object {
        @Volatile
        private var instance: Session? = null

        fun instance(): Session =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: Session()
                        .also { instance = it }
                newInstance
            }

        fun reset() {
            instance = null
        }
    }
}