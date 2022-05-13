package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class SendTrackRequest(
    val transportId: String,
    val kind: String,
    val rtpParameters: RtpParameters,
    val paused: Boolean,
    val appData: AppData,
    val peerId: String
)