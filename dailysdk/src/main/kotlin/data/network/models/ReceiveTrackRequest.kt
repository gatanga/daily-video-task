package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ReceiveTrackRequest(
    val mediaTag: String?,
    val mediaPeerId: String,
    val rtpCapabilities: RtpCapabilities,
    val peerId: String
)