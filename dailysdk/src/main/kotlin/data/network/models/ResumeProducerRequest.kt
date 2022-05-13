package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ResumeProducerRequest(val peerId: String, val producerId: String)