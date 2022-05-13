package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class TransportOptions(
    val id: String,
    val iceParameters: IceParameters,
    val iceCandidates: List<IceCandidate>,
    val dtlsParameters: DtlsParameters
)