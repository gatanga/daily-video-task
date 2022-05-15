package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ResumeProducerResponse(val resumed: Boolean? = null, val error: String? = null)