package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class DtlsParameters(
    val fingerprints: List<Fingerprint>,
    val role: String
)