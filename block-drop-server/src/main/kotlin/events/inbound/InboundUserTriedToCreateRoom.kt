package events.inbound

import kotlinx.serialization.*

@Serializable
data class InboundUserTriedToCreateRoom(val username: String)