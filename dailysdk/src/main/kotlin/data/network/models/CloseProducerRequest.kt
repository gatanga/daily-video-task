package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CloseProducerRequest(val peerId: String, val producerId: String)