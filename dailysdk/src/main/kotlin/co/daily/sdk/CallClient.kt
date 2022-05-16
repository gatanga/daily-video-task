package co.daily.sdk

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.work.WorkManager
import data.network.models.AppData
import data.network.models.CamAudio
import data.network.models.CamVideo
import data.network.models.TransportOptions
import data.result.RoomApiResult
import domain.repositories.ApiRepository
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.mediasoup.droid.*
import org.webrtc.AudioTrack
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import providers.network.RetrofitServiceProvider.Companion.json
import providers.repository.ApiRepositoryProvider
import providers.webrtc.EglBaseProvider
import providers.webrtc.PeerConnectionProvider
import session.Session
import utils.webrtc.DailyAudioManager
import utils.webrtc.WebRtcMediaUtils

class CallClient(private val context: Context, val width: Int, val height: Int) {

    private val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mainScope = CoroutineScope(Job() + Dispatchers.Main)
    private var repository: ApiRepository
    private lateinit var mediasoupDevice: Device
    private var sendTransport: SendTransport? = null
    private var recvTransport: RecvTransport? = null
    private var audioProducer: Producer? = null
    private var videoProducer: Producer? = null
    private var workHandler: Handler // worker looper handler.
    private var mainHandler: Handler // main looper handler.
    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private val consumers = mutableMapOf<String, MediaConsumer>()
    private var countDownTimer: CountDownTimer? = null
    private var recvTransportConnected = false
    private var audioManager: DailyAudioManager? = null
    private var remoteAudioAllowed = true
    private var remoteVideoAllowed = true
    private var frontFacing = true
    private var audioConsumerPaused = true
    private var videoConsumerPaused = true

    init {
        reset()
        DailySdk.startSdk(context = context.applicationContext)
        repository = ApiRepositoryProvider.apiRepository
        val workerThread = HandlerThread("worker")
        workerThread.start()
        workHandler = Handler(workerThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
        audioManager = DailyAudioManager.create(context = context)
        WebRtcMediaUtils.instance().setScreenDimensions(width = width, height = height)
    }

    private fun reset() {
        consumers.clear()
    }

    fun initializeLocalView(localView: SurfaceViewRenderer) {
        localVideoView = localView
        localVideoView.init(EglBaseProvider.instance().getEglBase().eglBaseContext, null)
//        localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
//        localVideoView.setEnableHardwareScaler(false)
        localVideoView.setMirror(true)
    }

    fun initializeRemoteView(remoteView: SurfaceViewRenderer) {
        remoteVideoView = remoteView
        remoteVideoView.init(EglBaseProvider.instance().getEglBase().eglBaseContext, null)
//        remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
//        remoteVideoView.setEnableHardwareScaler(false)
        remoteVideoView.setMirror(true)
    }

    fun createLocalMediaTracks() {
        createLocalAudioTrack()
        createLocalVideoTrack()
    }

    private fun createLocalAudioTrack() {
        runBlocking {
            WebRtcMediaUtils.instance().createAudioSource()
        }
        localAudioTrack = WebRtcMediaUtils.instance().createAudioTrack()
        localAudioTrack?.setEnabled(true)
    }

    private fun createLocalVideoTrack() {
        runBlocking {
            WebRtcMediaUtils.instance()
                .createVideoSource(context = context, frontFacing = frontFacing)
        }
        localVideoTrack = WebRtcMediaUtils.instance().createVideoTrack()
        localVideoTrack?.setEnabled(true)
        localVideoTrack?.addSink(localVideoView)
    }

    fun replaceVideoTrack() {
        Log.d(TAG, "replaceVideoTrack: $frontFacing")
        runBlocking {
            WebRtcMediaUtils.instance().switchCamera()
        }
        /*runBlocking {
            WebRtcMediaUtils.instance()
                .createVideoSource(context = context, frontFacing = frontFacing)
        }
        localVideoTrack?.removeSink(localVideoView)
        localVideoTrack?.dispose()
        localVideoTrack = WebRtcMediaUtils.instance().createVideoTrack()
        localVideoTrack?.setEnabled(true)
        localVideoTrack?.addSink(localVideoView)
        videoProducer?.replaceTrack(localVideoTrack)*/
        frontFacing = !frontFacing
    }

    fun toggleAudioOutputDevice() {
        audioManager?.toggleAudioOutputDevice()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun produceVideo() {
        try {
            videoProducer =
                sendTransport?.produce(
                    videoProduceListener,
                    localVideoTrack, null, null,
                    json.encodeToString(AppData(mediaTag = VIDEO_MEDIA_TAG))
                )
        } catch (e: MediasoupException) {
            Logger.e(TAG, "produceVideo: ${e.message}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun produceAudio() {
        try {
            audioProducer =
                sendTransport?.produce(
                    audioProduceListener,
                    localAudioTrack, null, null,
                    json.encodeToString(AppData(mediaTag = AUDIO_MEDIA_TAG))
                )
        } catch (e: MediasoupException) {
            Logger.e("produceAudio", "${e.message}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun joinRoom() {
        audioManager?.setAudioDevice(DailyAudioManager.AudioDevice.SPEAKER_PHONE)
        repository.joinRoom()
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        mediasoupDevice = Device()
                        mediasoupDevice.load(json.encodeToString(it.value.routerRtpCapabilities))
                    }
                    else -> {
                        Logger.e(TAG, "joinRoom: ", (it as RoomApiResult.Error).exception)
                        //FIXME -- We should return a proper type here
                        if (it.exception.message!!.contains("Network")) {
                            Toast.makeText(
                                context,
                                "We could not connect to the server",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        startSyncWork()
    }

    private fun startSyncWork() {
        /*val syncPeriodicWorker =
            PeriodicWorkRequestBuilder<PeerSyncWorker>(2, TimeUnit.SECONDS).addTag(WORKER_TAG)
                .build()
        WorkManager.getInstance(context).enqueue(syncPeriodicWorker)*/

        countDownTimer = object : CountDownTimer(9_000_000_000L, 2_000L) {
            override fun onTick(millisUntilFinished: Long) {
                ioScope.launch {
                    syncPeers()
                }
            }

            override fun onFinish() {

            }
        }.start()
    }

    suspend fun createTransports() {
        createSendTransport()
        createRecvTransport()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun createSendTransport() {
        repository.createTransport(direction = DIRECTION_SEND).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    createDeviceSendTransport(it.value.transportOptions)
                }
                else -> {
                    Logger.e(TAG, "createSendTransport: ", (it as RoomApiResult.Error).exception)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun connectTransport(transport: Transport, dtlsParameters: String) {
        repository.connectTransport(
            transportId = transport.id, dtlsParameters = dtlsParameters
        ).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    recvTransportConnected = true
                    Log.d(TAG, "connectTransport: success")
                }
                else -> Logger.e(TAG, "connectTransport: ", (it as RoomApiResult.Error).exception)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun createRecvTransport() {
        repository.createTransport(direction = DIRECTION_RECV)
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        createDeviceRecvTransport(options = it.value.transportOptions)
                    }
                    else -> {
                        Logger.e(
                            TAG,
                            "createRecvTransport: ",
                            (it as RoomApiResult.Error).exception
                        )
                    }
                }
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun sendTrack(
        transportId: String,
        kind: String,
        rtpParameters: String,
        appData: String
    ) {
        repository.sendTrack(
            transportId = transportId, kind = kind, rtpParameters = rtpParameters, appData = appData
        ).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG sendTrack: ", "success")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "createRecvTransport: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun processRemoteAudio(remotePeerId: String, camAudio: CamAudio) {
        if (camAudio.paused) {
            consumers[remotePeerId]?.audioConsumer?.let {
                if (!audioConsumerPaused) {
                    it.pause()
                    pauseConsumer(consumerId = it.id)
                    audioConsumerPaused = true
                }
            }
        } else {
            if (!consumers.containsKey(remotePeerId) || consumers[remotePeerId]?.audioConsumer == null) {
                if (!recvTransportConnected) {
                    return
                }
                if (!remoteAudioAllowed) {
                    return
                }
                repository.receiveTrack(
                    mediaTag = AUDIO_MEDIA_TAG,
                    mediaPeerId = remotePeerId,
                    rtpCapabilities = mediasoupDevice.rtpCapabilities
                ).collect {
                    when (it) {
                        is RoomApiResult.Success -> {
                            val response = it.value
                            consumers[remotePeerId] = MediaConsumer(
                                remotePeerId = remotePeerId,
                                audioConsumer =
                                recvTransport?.consume(
                                    {
                                        // FIXME -- We should close some consumer(s) here
                                    },
                                    response.id, response.producerId, response.kind,
                                    json.encodeToString(response.rtpParameters), null
                                ),
                                videoConsumer = consumers[remotePeerId]?.videoConsumer
                            )

                            consumers[remotePeerId]?.audioConsumer?.resume()
                            resumeConsumer(consumerId = consumers[remotePeerId]?.audioConsumer?.id!!)
                            audioConsumerPaused = false
                        }
                        else -> {
                            Logger.e(
                                TAG,
                                "receiveTrack: ",
                                (it as RoomApiResult.Error).exception
                            )
                        }
                    }
                }
            } else {
                consumers[remotePeerId]?.audioConsumer?.let {
                    if (audioConsumerPaused && !camAudio.paused) {
                        if (!remoteAudioAllowed) {
                            return
                        }
                        resumeConsumer(consumerId = it.id)
                        it.resume()
                        audioConsumerPaused = false
                    } else if (!audioConsumerPaused && camAudio.paused) {
                        pauseConsumer(consumerId = it.id)
                        it.pause()
                        audioConsumerPaused = true
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun processRemoteVideo(remotePeerId: String, camVideo: CamVideo) {
        /*if (camVideo.paused == consumers[remotePeerId]?.videoConsumer?.isPaused) {
            Log.d(TAG, "processRemoteVideo: No state change")
            return
        }*/
        if (camVideo.paused) {
            consumers[remotePeerId]?.videoConsumer?.let {
                if (!videoConsumerPaused) {
                    it.pause()
                    pauseConsumer(consumerId = it.id)
                    videoConsumerPaused = true
                    toggleVideoSink(remotePeerId = remotePeerId, enable = false)
                }
            }
        } else {
            if (!consumers.containsKey(remotePeerId) || consumers[remotePeerId]?.videoConsumer == null) {
                if (!recvTransportConnected) {
                    return
                }
                if (!remoteVideoAllowed) {
                    return
                }
                repository.receiveTrack(
                    mediaTag = VIDEO_MEDIA_TAG,
                    mediaPeerId = remotePeerId,
                    rtpCapabilities = mediasoupDevice.rtpCapabilities
                ).collect {
                    when (it) {
                        is RoomApiResult.Success -> {
                            val response = it.value
                            val videoConsumer = recvTransport?.consume(
                                { /*videoConsumer ->
                                            videoConsumer.close()
                                            consumers[remotePeerId] =
                                                consumers[remotePeerId]!!.copy(videoConsumer = null)
                                            if (consumers[remotePeerId]?.audioConsumer == null) {
                                                consumers.remove(remotePeerId)
                                            }
                                            closeConsumer(consumerId = videoConsumer.id)
                                            */
                                },
                                response.id, response.producerId, response.kind,
                                json.encodeToString(response.rtpParameters), null
                            )
                            consumers[remotePeerId] = MediaConsumer(
                                remotePeerId = remotePeerId,
                                videoConsumer = videoConsumer,
                                audioConsumer = consumers[remotePeerId]?.audioConsumer
                            )

                            resumeConsumer(consumerId = consumers[remotePeerId]?.videoConsumer?.id!!)
                            consumers[remotePeerId]?.videoConsumer?.resume()
                            videoConsumerPaused = false
                            toggleVideoSink(remotePeerId = remotePeerId, enable = true)
                        }
                        else -> {
                            Logger.e(
                                TAG,
                                "receiveTrack: ",
                                (it as RoomApiResult.Error).exception
                            )
                        }
                    }
                }
            } else {
                consumers[remotePeerId]?.videoConsumer?.let {
                    if (videoConsumerPaused && !camVideo.paused) {
                        if (!remoteVideoAllowed) {
                            return
                        }
                        resumeConsumer(consumerId = it.id)
                        it.resume()
                        videoConsumerPaused = false
                        toggleVideoSink(remotePeerId = remotePeerId, enable = true)
                    } else if (!videoConsumerPaused && camVideo.paused) {
                        pauseConsumer(consumerId = it.id)
                        it.pause()
                        videoConsumerPaused = true
                        toggleVideoSink(remotePeerId = remotePeerId, enable = false)
                    }
                }
            }
        }
    }

    private suspend fun closeConsumer(consumerId: String) {
        repository.closeConsumer(consumerId = consumerId).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG closeConsumer: ", "Consumer: $consumerId is closed")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "closeConsumer: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    private suspend fun pauseProducer(producerId: String) {
        repository.pauseProducer(producerId = producerId).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG pauseProducer: ", "Consumer: $producerId is paused")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "pauseProducer: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    private suspend fun resumeProducer(producerId: String) {
        repository.resumeProducer(producerId = producerId).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG resumeProducer: ", "Producer: $producerId is paused")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "resumeProducer: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    private suspend fun closeProducer(producerId: String) {
        repository.closeProducer(producerId = producerId).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG closeProducer: ", "Producer: $producerId is closed")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "closeProducer: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    private fun toggleVideoSink(remotePeerId: String, enable: Boolean) {
        Log.d(TAG, "toggleVideoSink: $enable")
        val videoTrack =
            consumers[remotePeerId]?.videoConsumer?.track as VideoTrack?
        mainScope.launch {
            videoTrack?.setEnabled(enable)
            if (enable) {
                remoteVideoView.visibility = View.VISIBLE
                videoTrack?.addSink(remoteVideoView)
            } else {
                remoteVideoView.visibility = View.GONE
                videoTrack?.removeSink(remoteVideoView)
            }
        }
    }

    suspend fun resumeConsumer(consumerId: String) {
        repository.resumeConsumer(consumerId = consumerId)
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        Log.d("$TAG resumeConsumer: ", "Success")
                    }
                    else -> {
                        Logger.e(
                            TAG,
                            "resumeConsumer: ",
                            (it as RoomApiResult.Error).exception
                        )
                    }
                }
            }
    }

    suspend fun pauseConsumer(consumerId: String) {
        repository.pauseConsumer(consumerId = consumerId).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d("$TAG resumeConsumer: ", "Success")
                }
                else -> {
                    Logger.e(
                        TAG,
                        "resumeConsumer: ",
                        (it as RoomApiResult.Error).exception
                    )
                }
            }
        }
    }

    suspend fun syncPeers() {
        if (Session.peerId.isNullOrEmpty()) {
            return
        }
        repository.syncPeers()
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        it.value.peers.entries.forEachIndexed { _, entry ->
                            /*if ((entry.value.media.camAudio != null || entry.value.media.camVideo != null)
                                && (consumers.isNotEmpty() || consumers.containsKey(entry.key))) {
                                Log.d(
                                    "${TAG}_syncPeers",
                                    "Currently, there is no support for multi-party call: ${entry.key}"
                                )
                            } else {*/
                            if (entry.key != Session.peerId) {
                                entry.value.media.camAudio?.let { camAudio ->
                                    ioScope.launch {
                                        processRemoteAudio(
                                            remotePeerId = entry.key,
                                            camAudio = camAudio
                                        )
                                    }
                                }

                                entry.value.media.camVideo?.let { camVideo ->
                                    ioScope.launch {
                                        delay(1_000L)
                                        processRemoteVideo(
                                            remotePeerId = entry.key,
                                            camVideo = camVideo
                                        )
                                    }
                                }
                            }
//                            }
                        }
                    }
                    else -> {
                        Logger.e(
                            TAG,
                            "syncPeers: ",
                            (it as RoomApiResult.Error).exception
                        )
                    }
                }
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createDeviceSendTransport(options: TransportOptions) {
        sendTransport = mediasoupDevice.createSendTransport(
            sendTransportListener,
            options.id,
            json.encodeToString(options.iceParameters),
            json.encodeToString(options.iceCandidates),
            json.encodeToString(options.dtlsParameters)
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createDeviceRecvTransport(options: TransportOptions) {
        recvTransport = mediasoupDevice.createRecvTransport(
            recvTransportListener,
            options.id,
            json.encodeToString(options.iceParameters),
            json.encodeToString(options.iceCandidates),
            json.encodeToString(options.dtlsParameters),
            null
        )
    }

    private val videoProduceListener = Producer.Listener {
        if (videoProducer != null) {
            videoProducer = null
        }
    }

    private val audioProduceListener = Producer.Listener {
        if (audioProducer != null) {
            audioProducer = null
        }
    }

    private val sendTransportListener = object : SendTransport.Listener {
        override fun onConnect(transport: Transport, dtlsParameters: String) {
            Log.d(TAG, "onConnect: $dtlsParameters")
            ioScope.launch {
                connectTransport(transport = transport, dtlsParameters = dtlsParameters)
            }
        }

        override fun onConnectionStateChange(transport: Transport, connectionState: String) {
            Logger.e(TAG, "onConnectionStateChange: $connectionState sendTransportListener")
        }

        override fun onProduce(
            transport: Transport?,
            kind: String?,
            rtpParameters: String?,
            appData: String?
        ): String {
            //FIXME -- Find out how to get the producerId

            ioScope.launch {
                sendTrack(
                    transportId = transport?.id!!,
                    kind = kind!!,
                    rtpParameters = rtpParameters!!,
                    appData = appData!!
                )
            }

            //FIXME -- Find out how to get the producerId
            return ""
        }
    }

    private val recvTransportListener = object : RecvTransport.Listener {
        override fun onConnect(transport: Transport, dtlsParameters: String) {
            Log.d(TAG, "onConnect: $dtlsParameters")
            ioScope.launch {
                connectTransport(transport = transport, dtlsParameters = dtlsParameters)
            }
        }

        override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
            Logger.d(TAG, "onConnectionStateChange: $connectionState recvTransportListener")
        }
    }

    val remoteAudioAvailable get() = consumers.values.firstOrNull()?.audioConsumer?.id?.isNotEmpty()

    val remoteVideoAvailable get() = consumers.values.firstOrNull()?.videoConsumer?.id?.isNotEmpty()

    private suspend fun toggleLocalAudio(enable: Boolean) {
        if (enable) {
            audioProducer?.let {
                resumeProducer(producerId = it.id)
                it.resume()
            }
        } else {
            audioProducer?.let {
                pauseProducer(producerId = it.id)
                it.pause()
            }
        }
    }

    private suspend fun toggleLocalVideo(enable: Boolean) {
        if (enable) {
            videoProducer?.let {
                resumeProducer(producerId = it.id)
                it.resume()
            }
        } else {
            videoProducer?.let {
                pauseProducer(producerId = it.id)
                it.pause()
            }
        }
    }

    private suspend fun toggleRemoteAudio(enable: Boolean) {
        remoteAudioAllowed = enable
        if (enable) {
            consumers.values.firstOrNull()?.audioConsumer?.let {
                resumeConsumer(consumerId = it.id)
                it.resume()
                audioConsumerPaused = false
            }
        } else {
            consumers.values.firstOrNull()?.audioConsumer?.let {
                pauseConsumer(consumerId = it.id)
                it.pause()
                audioConsumerPaused = true
            }
        }
    }

    private suspend fun toggleRemoteVideo(enable: Boolean) {
        remoteVideoAllowed = enable
        if (enable) {
            consumers.values.firstOrNull()?.videoConsumer?.let {
                resumeConsumer(consumerId = it.id)
                it.resume()
                videoConsumerPaused = false
            }
        } else {
            consumers.values.firstOrNull()?.videoConsumer?.let {
                pauseConsumer(consumerId = it.id)
                it.pause()
                videoConsumerPaused = true
            }
        }
    }

    suspend fun muteLocalAudio() {
        toggleLocalAudio(enable = false)
    }

    suspend fun unMuteLocalAudio() {
        toggleLocalAudio(enable = true)
    }

    suspend fun muteRemoteAudio() {
        toggleRemoteAudio(enable = false)
    }

    suspend fun unMuteRemoteAudio() {
        toggleRemoteAudio(enable = true)
    }

    suspend fun pauseLocalVideo() {
        toggleLocalVideo(enable = false)
        localVideoView.visibility = View.GONE
    }

    suspend fun unPauseLocalVideo() {
        toggleLocalVideo(enable = true)
        localVideoView.visibility = View.VISIBLE
    }

    suspend fun pauseRemoteVideo() {
        toggleRemoteVideo(enable = false)
        remoteVideoView.visibility = View.GONE
    }

    suspend fun unPauseRemoteVideo() {
        toggleRemoteVideo(enable = true)
        remoteVideoView.visibility = View.VISIBLE
    }

    private fun cancelWorkManager() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORKER_TAG)
    }

    private suspend fun closeConsumers() {
        consumers.forEach {
            it.value.videoConsumer?.id?.let { id ->
                closeConsumer(consumerId = id)
            }
            it.value.videoConsumer?.close()
            it.value.audioConsumer?.id?.let { id ->
                closeConsumer(consumerId = id)
            }
            it.value.audioConsumer?.close()
        }
    }

    private suspend fun closeProducers() {
        videoProducer?.let {
            closeProducer(producerId = it.id)
            it.close()
            videoProducer = null
        }

        audioProducer?.let {
            closeProducer(producerId = it.id)
            it.close()
            audioProducer = null
        }
    }

    private fun closeTracks() {
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        localVideoView.release()
        remoteVideoView.release()
    }

    fun clear() {
        ioScope.launch {
            audioManager?.close()
            countDownTimer?.cancel()
            WebRtcMediaUtils.instance().clear()
            PeerConnectionProvider.instance().closePeerConnection()
            cancelWorkManager()
            closeConsumers()
            closeProducers()
            closeTracks()
        }
    }

    companion object {
        private const val TAG = "CallClient"
        private const val VIDEO_MEDIA_TAG = "cam-video"
        private const val AUDIO_MEDIA_TAG = "cam-audio"
        private const val DIRECTION_SEND = "send"
        private const val DIRECTION_RECV = "recv"
        private const val WORKER_TAG = "PeerSyncWorker"

        @Volatile
        private var instance: CallClient? = null

        fun instance(context: Context, width: Int, height: Int): CallClient =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: CallClient(context = context, width = width, height = height)
                        .also { instance = it }
                newInstance
            }
    }
}