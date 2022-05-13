package data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Media(
    @SerialName("cam-video")
    val camVideo: CamVideo? = null,

    @SerialName("cam-audio")
    val camAudio: CamAudio? = null
)