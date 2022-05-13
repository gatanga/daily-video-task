package domain.repositories

import data.network.models.*
import data.network.rest.ApiManager
import data.network.rest.helper.ApiResponseWrapper
import data.result.RoomApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import providers.network.ApiManagerProvider

//FIXME -- Usually, in this layer, we'd need to map the api models to other type of models.
// But due to time constraints, I am not going to
class DailySdkApiRepository : ApiRepository {

    private val dispatcher = Dispatchers.IO
    private val apiManager: ApiManager by lazy { ApiManagerProvider.apiManager }

    override suspend fun joinRoom(request: BaseRequest): Flow<RoomApiResult<JoinRoomResponse>> {
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

    override suspend fun syncPeers(request: BaseRequest): Flow<RoomApiResult<SyncPeersResponse>> {
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

    override suspend fun createTransport(request: CreateTransportRequest): Flow<RoomApiResult<CreateTransportResponse>> {
        return flow {
            when (val response = apiManager.createTransport(request = request)) {
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

    override suspend fun connectTransport(request: ConnectTransportRequest): Flow<RoomApiResult<ConnectTransportResponse>> {
        return flow {
            when (val response = apiManager.connectTransport(request = request)) {
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

    override suspend fun sendTrack(request: SendTrackRequest): Flow<RoomApiResult<SendTrackResponse>> {
        return flow {
            when (val response = apiManager.sendTrack(request = request)) {
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

    override suspend fun receiveTrack(request: ReceiveTrackRequest): Flow<RoomApiResult<ReceiveTrackResponse>> {
        return flow {
            when (val response = apiManager.receiveTrack(request = request)) {
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

    override suspend fun resumeConsumer(request: ResumeConsumerRequest): Flow<RoomApiResult<ResumeConsumerResponse>> {
        return flow {
            when (val response = apiManager.resumeConsumer(request = request)) {
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

    override suspend fun resumeProducer(request: ResumeProducerRequest): Flow<RoomApiResult<ResumeProducerResponse>> {
        return flow {
            when (val response = apiManager.resumeProducer(request = request)) {
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

    override suspend fun pauseConsumer(request: PauseConsumerRequest): Flow<RoomApiResult<PauseConsumerResponse>> {
        return flow {
            when (val response = apiManager.pauseConsumer(request = request)) {
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

    override suspend fun pauseProducer(request: PauseProducerRequest): Flow<RoomApiResult<PauseProducerResponse>> {
        return flow {
            when (val response = apiManager.pauseProducer(request = request)) {
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

    override suspend fun closeConsumer(request: CloseConsumerRequest): Flow<RoomApiResult<CloseConsumerResponse>> {
        return flow {
            when (val response = apiManager.closeConsumer(request = request)) {
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

    override suspend fun closeProducer(request: CloseProducerRequest): Flow<RoomApiResult<CloseProducerResponse>> {
        return flow {
            when (val response = apiManager.closeProducer(request = request)) {
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
    }
}