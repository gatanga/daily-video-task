package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RtcpFeedback(val type: String, val parameter: String)