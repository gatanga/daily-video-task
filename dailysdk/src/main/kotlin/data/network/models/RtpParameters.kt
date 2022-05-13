package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RtpParameters(
    val codecs: List<Codec>,
    val headerExtensions: List<RtpParameterHeaderExtension>,
    val encodings: List<Encoding>,
    val rtcp: Rtcp,
    val mid: String
)
