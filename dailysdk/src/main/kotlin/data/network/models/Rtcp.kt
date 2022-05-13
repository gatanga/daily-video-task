package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Rtcp(
    val cname: String,
    val reducedSize: Boolean,
    val mux: Boolean? = null
)