package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RtpCapabilityHeaderExtension(
    val kind:String,
    val uri: String,
    val preferredId: Long,
    val preferredEncrypt: Boolean,
    val direction: String
)