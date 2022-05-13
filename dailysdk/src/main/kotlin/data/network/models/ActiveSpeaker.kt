package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ActiveSpeaker(
    val producerId: String? = null,
    val volume: Long? = null,
    val peerId: String? = null
)