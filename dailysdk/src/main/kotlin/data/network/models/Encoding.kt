package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Encoding(
    val active: Boolean? = null,
    val scaleResolutionDownBy: Long? = null,
    val maxBitrate: Long? = null,
    val rid: String? = null,
    val scalabilityMode: String? = null,
    val dtx: Boolean? = null,
    val ssrc: Long? = null/*,
    val rtx: Rtx? = null*/
)