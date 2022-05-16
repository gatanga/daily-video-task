package domain.models

data class IceTransportOptions(
    val id: String,
    val iceParameters: String,
    val iceCandidates: String,
    val dtlsParameters: String
)
