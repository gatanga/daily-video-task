package data.network.rest

import data.network.models.*
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {

    @POST("join-as-new-peer")
    suspend fun joinRoom(@Body request: BaseRequest): JoinRoomResponse

    @POST("sync")
    suspend fun syncPeers(@Body request: BaseRequest): SyncPeersResponse

    @POST("leave")
    suspend fun leaveRoom(@Body request: BaseRequest): LeaveRoomResponse

    @POST("create-transport")
    suspend fun createTransport(@Body request: CreateTransportRequest): CreateTransportResponse

    @POST("connect-transport")
    suspend fun connectTransport(@Body request: ConnectTransportRequest): ConnectTransportResponse

    @POST("send-track")
    suspend fun sendTrack(@Body request: SendTrackRequest): SendTrackResponse

    @POST("recv-track")
    suspend fun receiveTrack(@Body request: ReceiveTrackRequest): ReceiveTrackResponse

    @POST("resume-consumer")
    suspend fun resumeConsumer(@Body request: ResumeConsumerRequest): ResumeConsumerResponse

    @POST("resume-producer")
    suspend fun resumeProducer(@Body request: ResumeProducerRequest): ResumeProducerResponse

    @POST("pause-consumer")
    suspend fun pauseConsumer(@Body request: PauseConsumerRequest): PauseConsumerResponse

    @POST("pause-producer")
    suspend fun pauseProducer(@Body request: PauseProducerRequest): PauseProducerResponse

    @POST("close-consumer")
    suspend fun closeConsumer(@Body request: CloseConsumerRequest): CloseConsumerResponse

    @POST("close-producer")
    suspend fun closeProducer(@Body request: CloseProducerRequest): CloseProducerResponse
}