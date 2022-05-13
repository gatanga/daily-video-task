package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CloseConsumerResponse(val closed: String)