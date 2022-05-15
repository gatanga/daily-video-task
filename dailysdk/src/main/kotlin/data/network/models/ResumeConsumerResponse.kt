package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ResumeConsumerResponse(val resumed: Boolean? = null, val error: String? = null)