package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Stat(
    val bitrate: Long? = null,
    val fractionLost: Long? = null,
    val jitter: Long? = null,
    val score: Long? = null,
    val rid: String? = null
)