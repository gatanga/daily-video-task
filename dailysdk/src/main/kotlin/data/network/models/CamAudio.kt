package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CamAudio(
    val paused: Boolean,
    val encodings: List<Encoding>
)