package co.daily.sdk

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import data.network.models.*
import data.result.RoomApiResult
import domain.repositories.ApiRepository
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.mediasoup.droid.*
import org.webrtc.AudioTrack
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import providers.network.RetrofitServiceProvider.Companion.json
import providers.repository.ApiRepositoryProvider
import providers.webrtc.EglBaseProvider
import session.Session
import utils.webrtc.WebRtcMediaUtils

class CallClient(private val context: Context) {

    private lateinit var session: Session
    private val ioScope = CoroutineScope(Job() + Dispatchers.IO)
    private val mainScope = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var repository: ApiRepository
    private lateinit var mediasoupDevice: Device
    private var sendTransport: SendTransport? = null
    private var recvTransport: RecvTransport? = null
    private var audioProducer: Producer? = null
    private var videoProducer: Producer? = null
    private lateinit var workHandler: Handler // worker looper handler.
    private lateinit var mainHandler: Handler // main looper handler.
    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private val remotePeers = mutableMapOf<String, Peer>()
    private val consumers = mutableMapOf<String, MediaConsumer>()

    init {
        reset()
        DailySdk.startSdk(context = context.applicationContext)
        repository = ApiRepositoryProvider.apiRepository
        val workerThread = HandlerThread("worker")
        workerThread.start()
        workHandler = Handler(workerThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
    }

    private fun reset() {
        consumers.clear()
    }

    fun initializeLocalView(localView: SurfaceViewRenderer) {
        localVideoView = localView
        localVideoView.init(EglBaseProvider.instance().getEglBase().eglBaseContext, null)
        localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        localVideoView.setEnableHardwareScaler(false)
        localVideoView.setMirror(true)
    }

    fun initializeRemoteView(remoteView: SurfaceViewRenderer) {
        remoteVideoView = remoteView
        remoteVideoView.init(EglBaseProvider.instance().getEglBase().eglBaseContext, null)
        remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        remoteVideoView.setEnableHardwareScaler(false)
        remoteVideoView.setMirror(true)
    }

    fun createLocalMediaTracks() {
        createLocalAudioTrack()
        createLocalVideoTrack()
    }

    private fun createLocalAudioTrack() {
        runBlocking {
            WebRtcMediaUtils.instance().createAudioSource()
            localAudioTrack = WebRtcMediaUtils.instance().createAudioTrack()
            localAudioTrack?.setEnabled(true)
        }
    }

    private fun createLocalVideoTrack() {
        runBlocking {
            WebRtcMediaUtils.instance().createVideoSource(context = context)
            localVideoTrack = WebRtcMediaUtils.instance().createVideoTrack()
            localVideoTrack?.setEnabled(true)
            localVideoTrack?.addSink(localVideoView)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun produceVideo() {
        try {
            videoProducer = runBlocking {
                sendTransport?.produce(
                    videoProduceListener,
                    localVideoTrack, null, null,
                    json.encodeToString(AppData(mediaTag = VIDEO_MEDIA_TAG))
                )
            }
        } catch (e: MediasoupException) {
            Logger.e(TAG, "produceVideo: ${e.message}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun produceAudio() {
        try {
            audioProducer = runBlocking {
                sendTransport?.produce(
                    audioProduceListener,
                    localAudioTrack, null, null,
                    json.encodeToString(AppData(mediaTag = AUDIO_MEDIA_TAG))
                )
            }
        } catch (e: MediasoupException) {
            Logger.e("produceAudio", "${e.message}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun joinRoom() {
        Session.reset()
        session = Session.instance()
        repository.joinRoom(request = BaseRequest(peerId = session.peerId))
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        session.rtcpCapabilitiesJson =
                            json.encodeToString(it.value.routerRtpCapabilities)
                        mediasoupDevice = Device()
                        runBlocking {
                            mediasoupDevice.load(session.rtcpCapabilitiesJson)
                        }
                    }
                    else -> {
                        Logger.e(TAG, "joinRoom: ", (it as RoomApiResult.Error).exception)
                    }
                }
            }
        startSyncWork()
    }

    private fun startSyncWork() {
        //FIXME -- Fix the periodic worker
        /*val syncPeriodicWorker =
            PeriodicWorkRequestBuilder<PeerSyncWorker>(15, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueue(syncPeriodicWorker)*/

        object : CountDownTimer(9_000_000_000L, 15_000L) {
            override fun onTick(millisUntilFinished: Long) {
                runBlocking {
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
        repository.createTransport(
            request = CreateTransportRequest(peerId = session.peerId, direction = "send")
        ).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    session.sendTransportId = it.value.transportOptions.id
                    session.sendTransportJson = json.encodeToString(it.value)
                    runBlocking {
                        createDeviceSendTransport(it.value.transportOptions)
                    }
                }
                else -> {
                    Logger.e(TAG, "createSendTransport: ", (it as RoomApiResult.Error).exception)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun connectTransport(transport: Transport, dtlsParameters: String) {
        val dtlsParametersObj = json.decodeFromString<DtlsParameters>(dtlsParameters)
        repository.connectTransport(
            ConnectTransportRequest(
                transportId = transport.id,
                dtlsParameters = json.decodeFromString(dtlsParameters),
                peerId = session.peerId
            )
        ).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    Log.d(TAG, "connectTransport: success")
                }
                else -> Logger.e(TAG, "connectTransport: ", (it as RoomApiResult.Error).exception)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun createRecvTransport() {
        repository.createTransport(
            request = CreateTransportRequest(peerId = session.peerId, direction = DIRECTION_RECV)
        ).collect {
            when (it) {
                is RoomApiResult.Success -> {
                    runBlocking {
                        session.recvTransportId = it.value.transportOptions.id
                        session.recvTransportJson = json.encodeToString(it.value)
                        createDeviceRecvTransport(options = it.value.transportOptions)
                    }
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
            request = SendTrackRequest(
                transportId = transportId,
                kind = kind,
                rtpParameters = json.decodeFromString(rtpParameters),
                paused = false,
                appData = json.decodeFromString(appData),
                peerId = session.peerId
            )
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
                it.pause()
                pauseConsumer(consumerId = it.id)
            }
        } else {
            if (!consumers.containsKey(remotePeerId) || consumers[remotePeerId]?.audioConsumer == null) {
                repository.receiveTrack(
                    request = ReceiveTrackRequest(
                        mediaTag = AUDIO_MEDIA_TAG,
                        mediaPeerId = remotePeerId,
                        rtpCapabilities = json.decodeFromString(mediasoupDevice.rtpCapabilities),
                        peerId = session.peerId
                    )
                ).collect {
                    when (it) {
                        is RoomApiResult.Success -> {
                            runBlocking {
                                val response = it.value
                                consumers[remotePeerId] = MediaConsumer(
                                    remotePeerId = remotePeerId,
                                    audioConsumer = runBlocking {
                                        recvTransport?.consume(
                                            {

                                            },
                                            response.id, response.producerId, response.kind,
                                            json.encodeToString(response.rtpParameters), null
                                        )
                                    },
                                    videoConsumer = consumers[remotePeerId]?.videoConsumer
                                )

                                consumers[remotePeerId]?.audioConsumer?.resume()
                                resumeConsumer(consumerId = consumers[remotePeerId]?.audioConsumer?.id!!)
                            }
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
                    if (it.isPaused) {
                        resumeConsumer(consumerId = it.id)
                        it.resume()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun processRemoteVideo(remotePeerId: String, camVideo: CamVideo) {
        if (camVideo.paused) {
            consumers[remotePeerId]?.videoConsumer?.let {
                it.pause()
                pauseConsumer(consumerId = it.id)
                toggleVideoSink(remotePeerId = remotePeerId, enable = false)
            }
        } else {
            if (!consumers.containsKey(remotePeerId) || consumers[remotePeerId]?.videoConsumer == null) {
                repository.receiveTrack(
                    request = ReceiveTrackRequest(
                        mediaTag = VIDEO_MEDIA_TAG,
                        mediaPeerId = remotePeerId,
                        rtpCapabilities = json.decodeFromString(mediasoupDevice.rtpCapabilities),
                        peerId = session.peerId
                    )
                ).collect {
                    when (it) {
                        is RoomApiResult.Success -> {
                            runBlocking {
                                val response = it.value
                                val videoConsumer = runBlocking {
                                    recvTransport?.consume(
                                        { videoConsumer ->
                                            videoConsumer.close()
                                            consumers[remotePeerId] =
                                                consumers[remotePeerId]!!.copy(videoConsumer = null)
                                            if (consumers[remotePeerId]?.audioConsumer == null) {
                                                consumers.remove(remotePeerId)
                                            }
                                            runBlocking {
                                                closeConsumer(consumerId = videoConsumer.id)
                                            }
                                        },
                                        response.id, response.producerId, response.kind,
                                        json.encodeToString(response.rtpParameters), null
                                    )
                                }
                                consumers[remotePeerId] = MediaConsumer(
                                    remotePeerId = remotePeerId,
                                    videoConsumer = videoConsumer,
                                    audioConsumer = consumers[remotePeerId]?.audioConsumer
                                )

                                resumeConsumer(consumerId = consumers[remotePeerId]?.videoConsumer?.id!!)
                                consumers[remotePeerId]?.videoConsumer?.resume()
                                toggleVideoSink(remotePeerId = remotePeerId, enable = true)
                            }
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
                    if (it.isPaused) {
                        resumeConsumer(consumerId = it.id)
                        it.resume()
                        toggleVideoSink(remotePeerId = remotePeerId, enable = true)
                    }
                }
            }
        }
    }

    private suspend fun closeConsumer(consumerId: String) {
        repository.closeConsumer(
            request = CloseConsumerRequest(
                peerId = session.peerId,
                consumerId = consumerId
            )
        ).collect {
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

    private fun toggleVideoSink(remotePeerId: String, enable: Boolean) {
        val videoTrack =
            consumers[remotePeerId]?.videoConsumer?.track as VideoTrack?
        mainScope.launch {
            videoTrack?.setEnabled(enable)
            if (enable) {
                videoTrack?.addSink(remoteVideoView)
            } else {
                videoTrack?.removeSink(remoteVideoView)
            }
        }
    }

    suspend fun resumeConsumer(consumerId: String) {
        repository.resumeConsumer(
            request = ResumeConsumerRequest(
                peerId = session.peerId,
                consumerId = consumerId
            )
        )
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
        repository.pauseConsumer(
            request = PauseConsumerRequest(
                peerId = session.peerId,
                consumerId = consumerId
            )
        )
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

    suspend fun syncPeers() {
        if (!::session.isInitialized || session.peerId.isNullOrEmpty()) {
            return
        }

        repository.syncPeers(BaseRequest(peerId = session.peerId))
            .collect {
                when (it) {
                    is RoomApiResult.Success -> {
                        it.value.peers.entries.forEachIndexed { index, entry ->
                            /*if ((entry.value.media.camAudio != null || entry.value.media.camVideo != null)
                                && (consumers.isNotEmpty() || consumers.containsKey(entry.key))) {
                                Log.d(
                                    "${TAG}_syncPeers",
                                    "Currently, there is no support for multi-party call: ${entry.key}"
                                )
                            } else {*/
                            if (entry.key != session.peerId) {
                                entry.value.media.camAudio?.let { camAudio ->
                                    processRemoteAudio(
                                        remotePeerId = entry.key,
                                        camAudio = camAudio
                                    )
                                }

                                entry.value.media.camVideo?.let { camVideo ->
                                    processRemoteVideo(
                                        remotePeerId = entry.key,
                                        camVideo = camVideo
                                    )
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
        sendTransport = runBlocking {
            mediasoupDevice.createSendTransport(
                sendTransportListener,
                options.id,
                json.encodeToString(options.iceParameters),
                json.encodeToString(options.iceCandidates),
                json.encodeToString(options.dtlsParameters)
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createDeviceRecvTransport(options: TransportOptions) {
        recvTransport = runBlocking {
            mediasoupDevice.createRecvTransport(
                recvTransportListener,
                options.id,
                json.encodeToString(options.iceParameters),
                json.encodeToString(options.iceCandidates),
                json.encodeToString(options.dtlsParameters),
                null
            )
        }
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
            runBlocking {
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

            runBlocking {
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
            runBlocking {
                connectTransport(transport = transport, dtlsParameters = dtlsParameters)
            }
        }

        override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
            Logger.d(TAG, "onConnectionStateChange: $connectionState recvTransportListener")
        }
    }

    companion object {
        private const val TAG = "CallClient"
        private const val VIDEO_MEDIA_TAG = "cam-video"
        private const val AUDIO_MEDIA_TAG = "cam-audio"
        private const val DIRECTION_SEND = "send"
        private const val DIRECTION_RECV = "recv"

        @Volatile
        private var instance: CallClient? = null

        fun instance(context: Context): CallClient =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: CallClient(context = context)
                        .also { instance = it }
                newInstance
            }
    }
}