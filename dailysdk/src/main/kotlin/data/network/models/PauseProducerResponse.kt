package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class PauseProducerResponse(val paused: Boolean? = null, val error: String? = null)