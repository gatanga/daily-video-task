package co.daily.sdk

import org.mediasoup.droid.Consumer

data class MediaConsumer(
    val remotePeerId: String,
    val audioConsumer: Consumer?,
    val videoConsumer: Consumer?
)