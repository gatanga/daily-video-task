package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaveRoomResponse(val left: Boolean)