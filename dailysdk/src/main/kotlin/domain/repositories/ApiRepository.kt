package domain.repositories

import data.network.models.*
import data.result.RoomApiResult
import kotlinx.coroutines.flow.Flow

interface ApiRepository {

    suspend fun joinRoom(request: BaseRequest): Flow<RoomApiResult<JoinRoomResponse>>

    suspend fun syncPeers(request: BaseRequest): Flow<RoomApiResult<SyncPeersResponse>>

    suspend fun leaveRoom(request: BaseRequest): Flow<RoomApiResult<LeaveRoomResponse>>

    suspend fun createTransport(request: CreateTransportRequest): Flow<RoomApiResult<CreateTransportResponse>>

    suspend fun connectTransport(request: ConnectTransportRequest): Flow<RoomApiResult<ConnectTransportResponse>>

    suspend fun sendTrack(request: SendTrackRequest): Flow<RoomApiResult<SendTrackResponse>>

    suspend fun receiveTrack(request: ReceiveTrackRequest): Flow<RoomApiResult<ReceiveTrackResponse>>

    suspend fun resumeConsumer(request: ResumeConsumerRequest): Flow<RoomApiResult<ResumeConsumerResponse>>

    suspend fun resumeProducer(request: ResumeProducerRequest): Flow<RoomApiResult<ResumeProducerResponse>>

    suspend fun pauseConsumer(request: PauseConsumerRequest): Flow<RoomApiResult<PauseConsumerResponse>>

    suspend fun pauseProducer(request: PauseProducerRequest): Flow<RoomApiResult<PauseProducerResponse>>

    suspend fun closeConsumer(request: CloseConsumerRequest): Flow<RoomApiResult<CloseConsumerResponse>>

    suspend fun closeProducer(request: CloseProducerRequest): Flow<RoomApiResult<CloseProducerResponse>>
}