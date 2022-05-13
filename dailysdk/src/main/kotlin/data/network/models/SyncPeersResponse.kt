package data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class SyncPeersResponse(
    val peers: HashMap<String, Peer>,
    val activeSpeaker: ActiveSpeaker
)
