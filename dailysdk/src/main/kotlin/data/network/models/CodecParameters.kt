package data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodecParameters(
    val apt: Long? = null,

    @SerialName("level-asymmetry-allowed")
    val levelAsymmetryAllowed: Long? = null,

    @SerialName("packetization-mode")
    val packetizationMode: Long? = null,

    @SerialName("profile-level-id")
    val profileLevelId: String? = null,

    val minptime: Long? = null,
    val useinbandfec: Long? = null
)