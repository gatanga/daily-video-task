package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Peer(
    val joinTs: Long,
    val lastSeenTs: Long,
    val media: Media,
    val consumerLayers: ConsumerLayers,
    val stats: HashMap<String, List<Stat>>
)