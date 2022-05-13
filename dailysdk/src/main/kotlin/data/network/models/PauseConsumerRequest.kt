package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class PauseConsumerRequest(val peerId: String, val consumerId: String)