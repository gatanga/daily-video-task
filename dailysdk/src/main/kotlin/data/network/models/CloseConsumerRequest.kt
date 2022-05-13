package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CloseConsumerRequest(val peerId: String, val consumerId: String)