package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class IceParameters(
    val iceLite: Boolean,
    val password: String,
    val usernameFragment: String
)