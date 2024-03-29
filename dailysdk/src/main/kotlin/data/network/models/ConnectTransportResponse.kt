package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ConnectTransportResponse(val connected: Boolean? = null, val error: String? = null)