package co.daily.dailyvideoapp.android

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import co.daily.sdk.CallClient
import kotlinx.coroutines.runBlocking
import org.webrtc.SurfaceViewRenderer

class CallActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var callClient: CallClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)

        callClient = CallClient.instance(context = this)
        callClient.initializeLocalView(localView = localVideoView)
        callClient.initializeRemoteView(remoteView = remoteVideoView)

        callClient.createLocalMediaTracks()

        val handler = Handler()
        handler.postDelayed({
            runBlocking {
                callClient.joinRoom()
            }
            runBlocking {
                callClient.createTransports()
            }
            runBlocking {
                callClient.produceAudio()
            }
            runBlocking {
                callClient.produceVideo()
            }
        }, 5_000)
    }
}