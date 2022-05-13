package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RtpCapabilities(
    val codecs: List<Codec>,
    val headerExtensions: List<RtpCapabilityHeaderExtension>
)
