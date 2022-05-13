package providers.webrtc

import org.webrtc.EglBase

internal class EglBaseProvider private constructor() {
    private var eglBase: EglBase? = null

    fun getEglBase(): EglBase =
        synchronized(this) {
            return instance!!.eglBase!!
        }

    fun release() {
        synchronized(this) {
            if (instance != null) {
                instance!!.eglBase!!.release()
                instance!!.eglBase = null
                instance = null
            }
        }
    }

    companion object {

        @Volatile
        private var instance: EglBaseProvider? = null

        fun instance(): EglBaseProvider =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: EglBaseProvider()
                        .also { instance = it }
                newInstance
            }
    }

    init {
        eglBase = EglBase.create()
    }
}