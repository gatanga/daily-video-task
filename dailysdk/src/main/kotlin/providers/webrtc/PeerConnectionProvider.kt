package providers.webrtc

import org.webrtc.PeerConnectionFactory

class PeerConnectionProvider {

    var peerConnectionFactory: PeerConnectionFactory? =
        PeerConnectionFactory.builder().createPeerConnectionFactory()

    fun closePeerConnection() {
        if (peerConnectionFactory != null) {
            peerConnectionFactory?.dispose()
            peerConnectionFactory = null
            reset()
        }
    }

    companion object {

        const val MEDIA_STREAM_ID = "ARDAMS"

        @Volatile
        private var instance: PeerConnectionProvider? = null

        fun instance(): PeerConnectionProvider =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: PeerConnectionProvider()
                        .also { instance = it }
                newInstance
            }

        fun reset() {
            instance = null
        }
    }
}