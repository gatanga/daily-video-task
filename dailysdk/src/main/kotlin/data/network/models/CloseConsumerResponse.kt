package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CloseConsumerResponse(val closed: Boolean? = null, val error: String? = null)