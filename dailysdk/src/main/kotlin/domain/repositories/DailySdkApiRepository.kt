package domain.repositories

import android.content.Context
import data.network.models.*
import data.network.rest.ApiManager
import data.network.rest.helper.ApiResponseWrapper
import data.result.RoomApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import providers.network.ApiManagerProvider
import providers.network.RetrofitServiceProvider.Companion.json
import session.Session

//FIXME -- Usually, in this layer, we'd need to map the api models to other type of models.
// But due to time constraints, I am not going to
@OptIn(ExperimentalSerializationApi::class)
class DailySdkApiRepository(context: Context) : ApiRepository {

    private val dispatcher = Dispatchers.IO
    private val apiManager: ApiManager by lazy { ApiManagerProvider.apiManager }
    private val peerId: String = Session.getPeerId(context = context)
    private val request = BaseRequest(peerId = peerId)

    override suspend fun joinRoom(): Flow<RoomApiResult<JoinRoomResponse>> {

        return flow {
            when (val response = apiManager.joinRoom(request = request)) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun syncPeers(): Flow<RoomApiResult<SyncPeersResponse>> {
        return flow {
            when (val response = apiManager.syncPeers(request = request)) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun leaveRoom(request: BaseRequest): Flow<RoomApiResult<LeaveRoomResponse>> {
        return flow {
            when (val response = apiManager.leaveRoom(request = request)) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun createTransport(direction: String): Flow<RoomApiResult<CreateTransportResponse>> {
        return flow {
            when (val response = apiManager.createTransport(
                request
                = CreateTransportRequest(peerId = peerId, direction = direction)
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun connectTransport(transportId: String, dtlsParameters: String)
            : Flow<RoomApiResult<ConnectTransportResponse>> {
        return flow {
            when (val response = apiManager.connectTransport(
                request = ConnectTransportRequest(
                    transportId = transportId,
                    dtlsParameters = json.decodeFromString(dtlsParameters),
                    peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun sendTrack(
        transportId: String,
        kind: String,
        rtpParameters: String,
        appData: String
    ): Flow<RoomApiResult<SendTrackResponse>> {
        return flow {
            when (val response = apiManager.sendTrack(
                request = SendTrackRequest(
                    transportId = transportId,
                    kind = kind,
                    rtpParameters = json.decodeFromString(rtpParameters),
                    paused = false,
                    appData = json.decodeFromString(appData),
                    peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun receiveTrack(
        mediaTag: String,
        mediaPeerId: String,
        rtpCapabilities: String
    ): Flow<RoomApiResult<ReceiveTrackResponse>> {
        return flow {
            when (val response = apiManager.receiveTrack(
                request = ReceiveTrackRequest(
                    mediaTag = mediaTag,
                    mediaPeerId = mediaPeerId,
                    rtpCapabilities = json.decodeFromString(rtpCapabilities),
                    peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun resumeConsumer(consumerId: String): Flow<RoomApiResult<ResumeConsumerResponse>> {
        return flow {
            when (val response = apiManager.resumeConsumer(
                request = ResumeConsumerRequest(
                    peerId = peerId,
                    consumerId = consumerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun resumeProducer(producerId: String): Flow<RoomApiResult<ResumeProducerResponse>> {
        return flow {
            when (val response = apiManager.resumeProducer(
                request = ResumeProducerRequest(
                    peerId = peerId,
                    producerId = producerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun pauseConsumer(consumerId: String): Flow<RoomApiResult<PauseConsumerResponse>> {
        return flow {
            when (val response = apiManager.pauseConsumer(
                request = PauseConsumerRequest(
                    peerId = peerId,
                    consumerId = consumerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun pauseProducer(producerId: String): Flow<RoomApiResult<PauseProducerResponse>> {
        return flow {
            when (val response = apiManager.pauseProducer(
                request = PauseProducerRequest(
                    producerId = producerId,
                    peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun closeConsumer(consumerId: String): Flow<RoomApiResult<CloseConsumerResponse>> {
        return flow {
            when (val response = apiManager.closeConsumer(
                request = CloseConsumerRequest(
                    consumerId = consumerId,
                    peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun closeProducer(producerId: String): Flow<RoomApiResult<CloseProducerResponse>> {
        return flow {
            when (val response = apiManager.closeProducer(
                request = CloseProducerRequest(
                    producerId = producerId, peerId = peerId
                )
            )) {
                is ApiResponseWrapper.Success -> emit(RoomApiResult.Success(response.value))
                is ApiResponseWrapper.NetworkError -> emit(
                    RoomApiResult.Error(
                        Exception(
                            NETWORK_ERROR
                        )
                    )
                )
                else -> emit(RoomApiResult.Error(Exception(GENERIC_ERROR)))
            }
        }.flowOn(dispatcher)
    }

    companion object {
        private const val NETWORK_ERROR = "Network error"
        private const val GENERIC_ERROR = "Generic error"
        private const val VIDEO_MEDIA_TAG = "cam-video"
        private const val AUDIO_MEDIA_TAG = "cam-audio"
        private const val DIRECTION_SEND = "send"
        private const val DIRECTION_RECV = "recv"
        private const val WORKER_TAG = "PeerSyncWorker"
    }
}