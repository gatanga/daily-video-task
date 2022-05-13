package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ReceiveTrackResponse(
    val producerId: String,
    val id: String,
    val kind: String,
    val rtpParameters: RtpParameters,
    val type: String,
    val producerPaused: Boolean
)
