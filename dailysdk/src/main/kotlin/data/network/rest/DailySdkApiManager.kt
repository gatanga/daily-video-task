package data.network.rest

import data.network.models.*
import data.network.rest.helper.ApiResponseWrapper
import data.network.rest.helper.safeApiCall
import kotlinx.coroutines.Dispatchers
import providers.network.RetrofitServiceProvider

class DailySdkApiManager : ApiManager {

    private val dispatcher = Dispatchers.IO

    override suspend fun joinRoom(request: BaseRequest): ApiResponseWrapper<JoinRoomResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.joinRoom(request = request)
        }

    override suspend fun syncPeers(request: BaseRequest): ApiResponseWrapper<SyncPeersResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.syncPeers(request = request)
        }

    override suspend fun leaveRoom(request: BaseRequest): ApiResponseWrapper<LeaveRoomResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.leaveRoom(request = request)
        }

    override suspend fun createTransport(request: CreateTransportRequest): ApiResponseWrapper<CreateTransportResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.createTransport(request = request)
        }

    override suspend fun connectTransport(request: ConnectTransportRequest): ApiResponseWrapper<ConnectTransportResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.connectTransport(request = request)
        }

    override suspend fun sendTrack(request: SendTrackRequest): ApiResponseWrapper<SendTrackResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.sendTrack(request = request)
        }

    override suspend fun receiveTrack(request: ReceiveTrackRequest): ApiResponseWrapper<ReceiveTrackResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.receiveTrack(request = request)
        }

    override suspend fun resumeConsumer(request: ResumeConsumerRequest): ApiResponseWrapper<ResumeConsumerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.resumeConsumer(request = request)
        }

    override suspend fun resumeProducer(request: ResumeProducerRequest): ApiResponseWrapper<ResumeProducerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.resumeProducer(request = request)
        }

    override suspend fun pauseConsumer(request: PauseConsumerRequest): ApiResponseWrapper<PauseConsumerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.pauseConsumer(request = request)
        }

    override suspend fun pauseProducer(request: PauseProducerRequest): ApiResponseWrapper<PauseProducerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.pauseProducer(request = request)
        }

    override suspend fun closeConsumer(request: CloseConsumerRequest): ApiResponseWrapper<CloseConsumerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.closeConsumer(request = request)
        }

    override suspend fun closeProducer(request: CloseProducerRequest): ApiResponseWrapper<CloseProducerResponse> =
        safeApiCall(dispatcher = dispatcher) {
            RetrofitServiceProvider.retrofitService.closeProducer(request = request)
        }
}