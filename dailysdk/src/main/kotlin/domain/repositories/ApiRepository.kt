package domain.repositories

import data.network.models.*
import data.result.RoomApiResult
import kotlinx.coroutines.flow.Flow

interface ApiRepository {

    suspend fun joinRoom(): Flow<RoomApiResult<JoinRoomResponse>>

    suspend fun syncPeers(): Flow<RoomApiResult<SyncPeersResponse>>

    suspend fun leaveRoom(request: BaseRequest): Flow<RoomApiResult<LeaveRoomResponse>>

    suspend fun createTransport(direction: String): Flow<RoomApiResult<CreateTransportResponse>>

    suspend fun connectTransport(
        transportId: String, dtlsParameters: String
    ): Flow<RoomApiResult<ConnectTransportResponse>>

    suspend fun sendTrack(
        transportId: String, kind: String, rtpParameters: String, appData: String
    ): Flow<RoomApiResult<SendTrackResponse>>

    suspend fun receiveTrack(
        mediaTag: String,
        mediaPeerId: String,
        rtpCapabilities: String
    ): Flow<RoomApiResult<ReceiveTrackResponse>>

    suspend fun resumeConsumer(consumerId: String): Flow<RoomApiResult<ResumeConsumerResponse>>

    suspend fun resumeProducer(producerId: String): Flow<RoomApiResult<ResumeProducerResponse>>

    suspend fun pauseConsumer(consumerId: String): Flow<RoomApiResult<PauseConsumerResponse>>

    suspend fun pauseProducer(producerId: String): Flow<RoomApiResult<PauseProducerResponse>>

    suspend fun closeConsumer(consumerId: String): Flow<RoomApiResult<CloseConsumerResponse>>

    suspend fun closeProducer(producerId: String): Flow<RoomApiResult<CloseProducerResponse>>
}