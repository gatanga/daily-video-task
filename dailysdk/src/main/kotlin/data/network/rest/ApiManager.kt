package data.network.rest

import data.network.models.*
import data.network.rest.helper.ApiResponseWrapper

interface ApiManager {
    suspend fun joinRoom(request: BaseRequest): ApiResponseWrapper<JoinRoomResponse>

    suspend fun syncPeers(request: BaseRequest): ApiResponseWrapper<SyncPeersResponse>

    suspend fun leaveRoom(request: BaseRequest): ApiResponseWrapper<LeaveRoomResponse>

    suspend fun createTransport(request: CreateTransportRequest): ApiResponseWrapper<CreateTransportResponse>

    suspend fun connectTransport(request: ConnectTransportRequest): ApiResponseWrapper<ConnectTransportResponse>

    suspend fun sendTrack(request: SendTrackRequest): ApiResponseWrapper<SendTrackResponse>

    suspend fun receiveTrack(request: ReceiveTrackRequest): ApiResponseWrapper<ReceiveTrackResponse>

    suspend fun resumeConsumer(request: ResumeConsumerRequest): ApiResponseWrapper<ResumeConsumerResponse>

    suspend fun resumeProducer(request: ResumeProducerRequest): ApiResponseWrapper<ResumeProducerResponse>

    suspend fun pauseConsumer(request: PauseConsumerRequest): ApiResponseWrapper<PauseConsumerResponse>

    suspend fun pauseProducer(request: PauseProducerRequest): ApiResponseWrapper<PauseProducerResponse>

    suspend fun closeConsumer(request: CloseConsumerRequest): ApiResponseWrapper<CloseConsumerResponse>

    suspend fun closeProducer(request: CloseProducerRequest): ApiResponseWrapper<CloseProducerResponse>
}