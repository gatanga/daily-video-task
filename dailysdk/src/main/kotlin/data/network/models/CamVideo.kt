package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class CamVideo(
    val paused: Boolean,
    val encodings: List<Encoding>
)