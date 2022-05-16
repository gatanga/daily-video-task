package domain.models

data class RecvTransportOptions(
    val producerId: String,
    val id: String,
    val kind: String,
    val rtpParameters: String,
    val type: String
)
