package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CloseProducerResponse(val closed: String? = null, val error: String? = null)