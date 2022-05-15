package utils.webrtc

import android.content.Context
import org.mediasoup.droid.Logger
import org.webrtc.*
import providers.webrtc.EglBaseProvider
import providers.webrtc.PeerConnectionProvider

class WebRtcMediaUtils {

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null

    val mediaStream: MediaStream =
        PeerConnectionProvider.instance().peerConnectionFactory!!.createLocalMediaStream(
            PeerConnectionProvider.MEDIA_STREAM_ID
        )

    @Throws(Exception::class)
    fun createCameraCapturer(context: Context) {
        //This app is targeting sdk 24. If we need to support Android versions lower than Lollipop,
        // then we need to add the logic to select between Camera1Enumerator and Camera2Enumerator
        val cameraEnumerator = Camera2Enumerator(context)

        //I am using the front camera only for now. If you would like the feature to switch between
        // front and back facing camera, then change the logic below.
        cameraVideoCapturer = cameraEnumerator.createCapturer(
            cameraEnumerator.deviceNames.first { cameraEnumerator.isFrontFacing(it) },
            MediaCapturerEventHandler()
        )

        if (cameraVideoCapturer == null) {
            throw Exception("Failed to get Camera Device")
        }
    }

    fun createAudioSource() {
        if (audioSource != null) {
            return
        }
        Logger.d(TAG, "creating audio source")
        WebRtcThreadUtils.threadChecker.checkIsOnValidThread()
        audioSource = PeerConnectionProvider.instance().peerConnectionFactory!!.createAudioSource(
            MediaConstraints()
        )
        Logger.d(TAG, "Audio source created")
    }

    fun createVideoSource(context: Context) {
        if (videoSource != null) {
            return
        }
        Logger.d(TAG, "creating video source")
        if (cameraVideoCapturer == null) {
            createCameraCapturer(context = context)
        }

        WebRtcThreadUtils.threadChecker.checkIsOnValidThread()
        videoSource =
            PeerConnectionProvider.instance().peerConnectionFactory!!.createVideoSource(false)
        val surfaceTextureHelper = SurfaceTextureHelper.create(
            "CaptureThread",
            EglBaseProvider.instance().getEglBase().eglBaseContext
        )

        cameraVideoCapturer?.initialize(
            surfaceTextureHelper,
            context,
            videoSource?.capturerObserver
        )

        //We should set different values for width and height depending on the type of video
        // we want to send
        cameraVideoCapturer?.startCapture(640, 480, 30)
        Logger.d(TAG, "Video source created: ${cameraVideoCapturer != null}")
    }

    fun createAudioTrack(): AudioTrack {
        Logger.d(TAG, "Creating audio track")
        WebRtcThreadUtils.threadChecker.checkIsOnValidThread()

        val audioTrack = PeerConnectionProvider.instance().peerConnectionFactory!!.createAudioTrack(
            AUDIO_TRACK_ID,
            audioSource
        )
        Logger.d(TAG, "Audio track created: ${audioTrack != null}")

        return audioTrack
    }

    fun createVideoTrack(): VideoTrack {
        Logger.d(TAG, "Creating video track")
        WebRtcThreadUtils.threadChecker.checkIsOnValidThread()

        val videoTrack = PeerConnectionProvider.instance().peerConnectionFactory!!.createVideoTrack(
            VIDEO_TRACK_ID,
            videoSource
        )

        Logger.d(TAG, "Video track created: ${videoTrack != null}")

        return videoTrack
    }

    private class MediaCapturerEventHandler : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraOpening(s: String) {
            Logger.d(TAG, "onCameraOpening s=$s")
        }

        override fun onFirstFrameAvailable() {
            Logger.d(TAG, "onFirstFrameAvailable")
        }

        override fun onCameraFreezed(s: String) {
            Logger.d(TAG, "onCameraFreezed s=$s")
        }

        override fun onCameraError(s: String) {
            Logger.e(TAG, "onCameraError s=$s")
        }

        override fun onCameraDisconnected() {
            Logger.d(TAG, "onCameraDisconnected")
        }

        override fun onCameraClosed() {
            Logger.d(TAG, "onCameraClosed")
        }
    }

    fun clear() {
        audioSource?.dispose()
        audioSource = null
        videoSource?.dispose()
        videoSource = null
        mediaStream.dispose()
    }

    companion object {

        private const val TAG = "WebRtcMediaUtils"
        private const val VIDEO_TRACK_ID = "ARDAMSv0"
        private const val AUDIO_TRACK_ID = "ARDAMSa0"

        @Volatile
        private var instance: WebRtcMediaUtils? = null

        fun instance(): WebRtcMediaUtils =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: WebRtcMediaUtils()
                        .also { instance = it }
                newInstance
            }
    }
}