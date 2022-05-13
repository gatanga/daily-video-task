package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ResumeConsumerRequest(val peerId: String, val consumerId: String)