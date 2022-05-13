package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class IceCandidate(
    val foundation: String,
    val ip: String,
    val port: Long,
    val priority: Long,
    val protocol: String,
    val type: String,
    val tcpType: String? = null
)
