package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class JoinRoomResponse(
    val routerRtpCapabilities: RtpCapabilities
)