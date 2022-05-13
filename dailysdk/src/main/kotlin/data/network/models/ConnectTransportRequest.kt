package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ConnectTransportRequest(
    val transportId: String,
    val dtlsParameters: DtlsParameters,
    val peerId: String
)