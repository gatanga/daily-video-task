package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class PauseConsumerResponse(val paused: Boolean? = null, val error: String? = null)