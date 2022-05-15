package co.daily.dailyvideoapp.android

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import co.daily.sdk.CallClient
import kotlinx.coroutines.*
import org.webrtc.SurfaceViewRenderer

class CallActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var callClient: CallClient
    val handler = Handler()
    private val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    private var localAudioOn = true
    private var localVideoOn = true
    private var remoteAudioOn = true
    private var remoteVideoOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)

        callClient = CallClient.instance(context = this)
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
        val alertDialog: AlertDialog.Builder = Builder(this@CallActivity)
        val items = arrayOf(
            getString(R.string.local_audio),
            getString(R.string.local_video),
            getString(R.string.subscribe_to_remote_audio),
            getString(R.string.subscribe_to_remote_video)
        )
        val checkedItems =
            booleanArrayOf(localAudioOn, localVideoOn, remoteAudioOn, remoteVideoOn)
        alertDialog.setMultiChoiceItems(
            items, checkedItems
        ) { _, which, isChecked ->
            when (which) {
                0 -> {
                    localAudioOn = isChecked
                    if (!isChecked) {
                        ioScope.launch {
                            callClient.muteLocalAudio()
                        }
                    } else {
                        ioScope.launch {
                            callClient.unMuteLocalAudio()
                        }
                    }
                }
                1 -> {
                    localVideoOn = isChecked
                    if (!isChecked) {
                        ioScope.launch {
                            callClient.pauseLocalVideo()
                        }
                    } else {
                        ioScope.launch {
                            callClient.unPauseLocalVideo()
                        }
                    }
                }
                2 -> {
                    remoteAudioOn = isChecked
                    if (!isChecked) {
                        ioScope.launch {
                            callClient.muteRemoteAudio()
                        }
                    } else {
                        ioScope.launch {
                            callClient.unMuteRemoteAudio()
                        }
                    }
                }
                3 -> {
                    remoteVideoOn = isChecked
                    if (!isChecked) {
                        ioScope.launch {
                            callClient.pauseRemoteVideo()
                        }
                    } else {
                        ioScope.launch {
                            callClient.unPauseRemoteVideo()
                        }
                    }
                }
            }
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callClient.clear()
    }
}