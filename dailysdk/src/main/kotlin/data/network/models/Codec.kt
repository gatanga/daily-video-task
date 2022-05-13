package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Codec(
    val kind: String? = null,
    val mimeType: String,
    val clockRate: Long,
    val channels: Long? = null,
    val rtcpFeedback: List<RtcpFeedback>,
    val parameters: CodecParameters,
    val preferredPayloadType: Long? = null,
    val payloadType: Long? = null
)