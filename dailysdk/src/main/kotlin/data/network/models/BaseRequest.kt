package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class BaseRequest(val peerId: String)