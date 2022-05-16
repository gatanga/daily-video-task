package domain.mappers

import data.network.models.CreateTransportResponse
import data.network.models.ReceiveTrackResponse
import data.network.models.SyncPeersResponse
import domain.models.IceTransportOptions
import domain.models.Media
import domain.models.RecvTransportOptions
import domain.models.RemotePeer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import providers.network.RetrofitServiceProvider.Companion.json

fun SyncPeersResponse.remotePeers(localPeerId: String) =
    this.peers.filter { it.key != localPeerId }.entries.map {
        RemotePeer(
            peerId = it.key,
            audio = Media(paused = it.value.media.camAudio != null && it.value.media.camAudio!!.paused),
            video = Media(paused = it.value.media.camVideo != null && it.value.media.camVideo!!.paused)
        )
    }

@OptIn(ExperimentalSerializationApi::class)
fun CreateTransportResponse.options(): IceTransportOptions =
    IceTransportOptions(
        id = this.transportOptions.id,
        iceParameters = json.encodeToString(this.transportOptions.iceParameters),
        iceCandidates = json.encodeToString(this.transportOptions.iceCandidates),
        dtlsParameters = json.encodeToString(this.transportOptions.dtlsParameters)
    )

@OptIn(ExperimentalSerializationApi::class)
fun ReceiveTrackResponse.recvTransportOptions() = RecvTransportOptions(
    producerId = this.producerId,
    id = this.id,
    kind = this.kind,
    rtpParameters = json.encodeToString(this.rtpParameters),
    type = this.type
)