package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class PauseProducerRequest(val peerId: String, val producerId: String)