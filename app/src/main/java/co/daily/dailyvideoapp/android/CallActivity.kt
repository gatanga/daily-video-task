package co.daily.dailyvideoapp.android

import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import co.daily.sdk.CallClient
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import org.webrtc.SurfaceViewRenderer

class CallActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var callClient: CallClient
    val handler = Handler()
    private val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mainScope = CoroutineScope(Job() + Dispatchers.Main)
    private var localAudioOn = true
    private var localVideoOn = true
    private var remoteAudioOn = true
    private var remoteVideoOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        callClient = CallClient.instance(context = this, width, height)
        callClient.initializeLocalView(localView = localVideoView)
        callClient.initializeRemoteView(remoteView = remoteVideoView)

        callClient.createLocalMediaTracks()


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

    private fun showMediaStreamToggleDialog() {
        val remoteAudioAvailable = callClient.remoteAudioAvailable
        val remoteVideoAvailable = callClient.remoteVideoAvailable
        val dialog = MaterialDialog(this).show {
            customView(R.layout.call_options, /*scrollable = true, */horizontalPadding = true)
        }

        val localAudioCheckBox =
            dialog.view.findViewById<MaterialCheckBox>(R.id.mute_local_audio)
        val localAudioText =
            dialog.view.findViewById<MaterialTextView>(R.id.mute_local_audio_text)

        val localVideoCheckBox =
            dialog.view.findViewById<MaterialCheckBox>(R.id.pause_local_video)
        val localVideoText =
            dialog.view.findViewById<MaterialTextView>(R.id.pause_local_video_text)

        val remoteAudioCheckBox =
            dialog.view.findViewById<MaterialCheckBox>(R.id.mute_remote_audio)
        val remoteAudioText =
            dialog.view.findViewById<MaterialTextView>(R.id.mute_remote_audio_text)

        if (remoteAudioAvailable != true) {
            remoteAudioCheckBox.visibility = View.GONE
            remoteAudioText.visibility = View.GONE
        }

        val remoteVideoCheckBox =
            dialog.view.findViewById<MaterialCheckBox>(R.id.pause_remote_video)
        val remoteVideoText =
            dialog.view.findViewById<MaterialTextView>(R.id.pause_remote_video_text)

        if (remoteVideoAvailable != true) {
            remoteVideoCheckBox.visibility = View.GONE
            remoteVideoText.visibility = View.GONE
        }

        localAudioCheckBox.isChecked = localAudioOn
        localVideoCheckBox.isChecked = localVideoOn
        remoteAudioCheckBox.isChecked = remoteAudioOn && (remoteAudioAvailable == true)
        remoteVideoCheckBox.isChecked = remoteVideoOn && (remoteVideoAvailable == true)

        localAudioCheckBox.setOnCheckedChangeListener { _, checked ->
            toggleLocalAudio(enable = checked)
        }
        localAudioText.setOnClickListener {
            localAudioCheckBox.isChecked = localAudioOn
            toggleLocalAudio(enable = localAudioOn)
        }
        localVideoCheckBox.setOnCheckedChangeListener { _, checked ->
            toggleLocalVideo(enable = checked)
        }
        localVideoText.setOnClickListener {
            localVideoCheckBox.isChecked = localVideoOn
            toggleLocalVideo(enable = localVideoOn)
        }
        remoteAudioCheckBox.setOnCheckedChangeListener { _, checked ->
            toggleRemoteAudio(enable = checked)
        }
        remoteAudioText.setOnClickListener {
            remoteAudioCheckBox.isChecked = remoteAudioOn
            toggleRemoteAudio(enable = remoteAudioOn)
        }
        remoteVideoCheckBox.setOnCheckedChangeListener { _, checked ->
            toggleRemoteVideo(enable = checked)
        }
        remoteVideoText.setOnClickListener {
            remoteVideoCheckBox.isChecked = remoteVideoOn
            toggleRemoteVideo(enable = remoteVideoOn)
        }
    }

    private fun toggleLocalAudio(enable: Boolean) {
        localAudioOn = enable
        if (!enable) {
            mainScope.launch {
                callClient.muteLocalAudio()
            }
        } else {
            mainScope.launch {
                callClient.unMuteLocalAudio()
            }
        }
    }

    private fun toggleLocalVideo(enable: Boolean) {
        localVideoOn = enable
        if (!enable) {
            mainScope.launch {
                callClient.pauseLocalVideo()
            }
        } else {
            mainScope.launch {
                callClient.unPauseLocalVideo()
            }
        }
    }

    private fun toggleRemoteAudio(enable: Boolean) {
        remoteAudioOn = enable
        if (!enable) {
            mainScope.launch {
                callClient.muteRemoteAudio()
            }
        } else {
            mainScope.launch {
                callClient.unMuteRemoteAudio()
            }
        }
    }

    private fun toggleRemoteVideo(enable: Boolean) {
        remoteVideoOn = enable
        if (!enable) {
            mainScope.launch {
                callClient.pauseRemoteVideo()
            }
        } else {
            mainScope.launch {
                callClient.unPauseRemoteVideo()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_call, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.call_settings -> {
                showMediaStreamToggleDialog()
                true
            }
            R.id.switch_camera -> {
                mainScope.launch {
                    callClient.replaceVideoTrack()
                }
                true
            }
            /*R.id.loudspeaker -> {
                mainScope.launch {
                    callClient.toggleAudioOutputDevice()
                }
                true
            }*/
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callClient.clear()
    }
}