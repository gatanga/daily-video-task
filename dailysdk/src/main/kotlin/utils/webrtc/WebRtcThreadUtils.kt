package utils.webrtc

import org.webrtc.ThreadUtils

class WebRtcThreadUtils {
    companion object {
        val threadChecker = ThreadUtils.ThreadChecker()
    }
}