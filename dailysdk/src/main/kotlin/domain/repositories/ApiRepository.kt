package domain.repositories

import data.network.models.*
import data.result.RoomApiResult
import domain.models.IceTransportOptions
import domain.models.RecvTransportOptions
import domain.models.RemotePeer
import kotlinx.coroutines.flow.Flow

interface ApiRepository {

    suspend fun joinRoom(): Flow<RoomApiResult<String>>

    suspend fun syncPeers(): Flow<RoomApiResult<List<RemotePeer>>>

    suspend fun leaveRoom(request: BaseRequest): Flow<RoomApiResult<LeaveRoomResponse>>

    suspend fun createTransport(direction: String): Flow<RoomApiResult<IceTransportOptions>>

    suspend fun connectTransport(
        transportId: String, dtlsParameters: String
    ): Flow<RoomApiResult<Boolean>>

    suspend fun sendTrack(
        transportId: String, kind: String, rtpParameters: String, appData: String
    ): Flow<RoomApiResult<String>>

    suspend fun receiveTrack(
        mediaTag: String,
        mediaPeerId: String,
        rtpCapabilities: String
    ): Flow<RoomApiResult<RecvTransportOptions>>

    suspend fun resumeConsumer(consumerId: String): Flow<RoomApiResult<Boolean?>>

    suspend fun resumeProducer(producerId: String): Flow<RoomApiResult<Boolean?>>

    suspend fun pauseConsumer(consumerId: String): Flow<RoomApiResult<Boolean?>>

    suspend fun pauseProducer(producerId: String): Flow<RoomApiResult<Boolean?>>

    suspend fun closeConsumer(consumerId: String): Flow<RoomApiResult<Boolean?>>

    suspend fun closeProducer(producerId: String): Flow<RoomApiResult<Boolean?>>
}