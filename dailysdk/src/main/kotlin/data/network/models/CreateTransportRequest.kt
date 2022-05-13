package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransportRequest(
    val peerId: String,
    val direction: String
)