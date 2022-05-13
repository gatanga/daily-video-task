package data.network.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RtpParameterHeaderExtension(
    val uri: String,
    val id: Long,
    val encrypt: Boolean,
    val parameters: JsonObject
)