package utils.webrtc

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import org.mediasoup.droid.Logger
import org.webrtc.*
import providers.webrtc.EglBaseProvider
import providers.webrtc.PeerConnectionProvider

class WebRtcMediaUtils {

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var width: Int? = null
    private var height: Int? = null

    val mediaStream: MediaStream =
        PeerConnectionProvider.instance().peerConnectionFactory!!.createLocalMediaStream(
            PeerConnectionProvider.MEDIA_STREAM_ID
        )

    @Throws(Exception::class)
    fun createCameraCapturer(context: Context, frontFacing: Boolean = true) {
        //This app is targeting sdk 24. If we need to support Android versions lower than Lollipop,
        // then we need to add the logic to select between Camera1Enumerator and Camera2Enumerator
        val cameraEnumerator = Camera2Enumerator(context)

        //I am using the front camera only for now. If you would like the feature to switch between
        // front and back facing camera, then change the logic below.
        cameraVideoCapturer = cameraEnumerator.createCapturer(
            cameraEnumerator.deviceNames.first {
                if (frontFacing) cameraEnumerator.isFrontFacing(it) else !cameraEnumerator.isFrontFacing(
                    it
                )
            },
            MediaCapturerEventHandler()
        )

        if (cameraVideoCapturer == null) {
            throw Exception("Failed to get Camera Device")
        }
    }

    fun switchCamera() {
        cameraVideoCapturer?.switchCamera(CameraSwitchHandler())
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

    fun createVideoSource(context: Context, frontFacing: Boolean) {
        Logger.d(TAG, "creating video source")
        createCameraCapturer(context = context, frontFacing = frontFacing)

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
        cameraVideoCapturer?.startCapture(640, 480, 60)
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

    private class CameraSwitchHandler : CameraVideoCapturer.CameraSwitchHandler {
        override fun onCameraSwitchDone(p0: Boolean) {
            Log.d(TAG, "onCameraSwitchDone: $p0")
        }

        override fun onCameraSwitchError(p0: String?) {
            Log.d(TAG, "onCameraSwitchError: $p0")
        }
    }

    fun clear() {
        audioSource?.dispose()
        audioSource = null
        videoSource?.dispose()
        videoSource = null
        cameraVideoCapturer?.stopCapture()
        cameraVideoCapturer?.dispose()
        cameraVideoCapturer = null
    }

    fun setScreenDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
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