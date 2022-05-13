package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransportResponse(
    val transportOptions: TransportOptions
)
