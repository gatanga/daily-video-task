package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Fingerprint(
    val algorithm: String,
    val value: String
)