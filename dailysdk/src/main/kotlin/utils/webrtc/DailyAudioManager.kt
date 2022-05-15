/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package utils.webrtc

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.FEATURE_TELEPHONY
import android.media.AudioManager
import android.util.Log

/**
 * DailyAudioManager manages all audio related parts of the AppRTC demo.
 */
class DailyAudioManager private constructor(
    private val context: Context
) {
    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    // TODO(henrika): add support for BLUETOOTH as well.
    enum class AudioDevice {
        SPEAKER_PHONE, WIRED_HEADSET, EARPIECE
    }

    private var initialized = false
    private val audioManager: AudioManager
    private var savedAudioMode: Int = AudioManager.MODE_INVALID
    private var savedIsSpeakerPhoneOn = false
    private var savedIsMicrophoneMute = false
    private var defaultAudioDevice: AudioDevice? = null

    // Contains speakerphone setting: auto, true or false
    private var useSpeakerphone: String? = null

    /**
     * Returns the currently selected audio device.
     */
    // Contains the currently selected audio device.
    var selectedAudioDevice: AudioDevice? = null
        private set

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private val audioDevices: MutableSet<AudioDevice?> = HashSet()

    // Broadcast receiver for wired headset intent broadcasts.
    private var wiredHeadsetReceiver: BroadcastReceiver? = null

    // Callback method for changes in audio focus.
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    fun init() {
        Log.d(TAG, "init")
        if (initialized) {
            return
        }

        // Store current audio state so we can restore it when close() is called.
        savedAudioMode = audioManager.mode
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn
        savedIsMicrophoneMute = audioManager.isMicrophoneMute

        // Request audio playout focus (without ducking) and install listener for changes in focus.
        val result: Int = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams")
        } else {
            Log.e(TAG, "Audio focus request failed")
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        // TODO(henrika): we migh want to start with RINGTONE mode here instead.
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false)

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a wired headset or by covering/uncovering the
        // proximity sensor.
        updateAudioDeviceState(hasWiredHeadset())

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset (Intent.ACTION_HEADSET_PLUG).
        registerForWiredHeadsetIntentBroadcast()
        initialized = true
    }

    @SuppressLint("WrongConstant")
    fun close() {
        Log.d(TAG, "close")
        if (!initialized) {
            return
        }
        unregisterForWiredHeadsetIntentBroadcast()

        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn)
        setMicrophoneMute(savedIsMicrophoneMute)
        audioManager.mode = savedAudioMode

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        audioManager.abandonAudioFocus(audioFocusChangeListener)
        audioFocusChangeListener = null
        initialized = false
    }

    /**
     * Changes selection of the currently active audio device.
     */
    fun setAudioDevice(device: AudioDevice?) {
        Log.d(TAG, "setAudioDevice(device=$device)")
        when (device) {
            AudioDevice.SPEAKER_PHONE -> {
                setSpeakerphoneOn(true)
                selectedAudioDevice = AudioDevice.SPEAKER_PHONE
            }
            AudioDevice.EARPIECE -> {
                setSpeakerphoneOn(false)
                selectedAudioDevice = AudioDevice.EARPIECE
            }
            AudioDevice.WIRED_HEADSET -> {
                setSpeakerphoneOn(false)
                selectedAudioDevice = AudioDevice.WIRED_HEADSET
            }
            else -> Log.e(TAG, "Invalid audio device selection")
        }
    }

    /**
     * Registers receiver for the broadcasted intent when a wired headset is
     * plugged in or unplugged. The received intent will have an extra
     * 'state' value where 0 means unplugged, and 1 means plugged.
     */
    private fun registerForWiredHeadsetIntentBroadcast() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        /** Receiver which handles changes in wired headset availability.  */
        wiredHeadsetReceiver = object : BroadcastReceiver() {
            private val STATE_UNPLUGGED = 0
            private val STATE_PLUGGED = 1
            private val HAS_NO_MIC = 0
            private val HAS_MIC = 1

            override fun onReceive(context: Context, intent: Intent) {
                val state: Int = intent.getIntExtra("state", STATE_UNPLUGGED)
                val microphone: Int = intent.getIntExtra("microphone", HAS_NO_MIC)
                val name: String? = intent.getStringExtra("name")
                Log.d(
                    TAG,
                    "BroadcastReceiver.onReceive" + ": " + "a=" + intent.action
                        .toString() + ", s=" + (if (state == STATE_UNPLUGGED) "unplugged" else "plugged").toString() + ", m=" + (if (microphone == HAS_MIC) "mic" else "no mic").toString() + ", n=" + name + ", sb="
                            + isInitialStickyBroadcast
                )
                val hasWiredHeadset = state == STATE_PLUGGED
                when (state) {
                    STATE_UNPLUGGED -> updateAudioDeviceState(hasWiredHeadset)
                    STATE_PLUGGED -> if (selectedAudioDevice != AudioDevice.WIRED_HEADSET) {
                        updateAudioDeviceState(hasWiredHeadset)
                    }
                    else -> Log.e(TAG, "Invalid state")
                }
            }
        }
        context.registerReceiver(wiredHeadsetReceiver, filter)
    }

    /**
     * Unregister receiver for broadcasted ACTION_HEADSET_PLUG intent.
     */
    private fun unregisterForWiredHeadsetIntentBroadcast() {
        try {
            context.unregisterReceiver(wiredHeadsetReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        wiredHeadsetReceiver = null
    }

    /**
     * Sets the speaker phone mode.
     */
    fun setSpeakerphoneOn(on: Boolean) {
        val wasOn: Boolean = audioManager.isSpeakerphoneOn()
        if (wasOn == on) {
            return
        }
        audioManager.setSpeakerphoneOn(on)
    }

    /**
     * Sets the microphone mute state.
     */
    private fun setMicrophoneMute(on: Boolean) {
        val wasMuted: Boolean = audioManager.isMicrophoneMute()
        if (wasMuted == on) {
            return
        }
        audioManager.setMicrophoneMute(on)
    }

    /**
     * Gets the current earpiece state.
     */
    private fun hasEarpiece(): Boolean {
        return context.packageManager.hasSystemFeature(FEATURE_TELEPHONY)
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated("")
    private fun hasWiredHeadset(): Boolean {
        return audioManager.isWiredHeadsetOn()
    }

    /**
     * Update list of possible audio devices and make new device selection.
     */
    private fun updateAudioDeviceState(hasWiredHeadset: Boolean) {
        // Update the list of available audio devices.
        audioDevices.clear()
        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            audioDevices.add(AudioDevice.WIRED_HEADSET)
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            audioDevices.add(AudioDevice.SPEAKER_PHONE)
            if (hasEarpiece()) {
                audioDevices.add(AudioDevice.EARPIECE)
            }
        }
        Log.d(TAG, "audioDevices: $audioDevices")

        // Switch to correct audio device given the list of available audio devices.
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET)
        } else {
            setAudioDevice(defaultAudioDevice)
        }
    }

    companion object {
        private const val TAG = "DailyAudioManager"
        /**
         * Construction
         */
        fun create(
            context: Context,
        ): DailyAudioManager {
            return DailyAudioManager(context)
        }
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        useSpeakerphone = "true"
        defaultAudioDevice = AudioDevice.SPEAKER_PHONE
    }
}