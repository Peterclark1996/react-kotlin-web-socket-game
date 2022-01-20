package events.inbound

import kotlinx.serialization.*

@Serializable
data class InboundUserTriedToJoinRoom(val room: String, val username: String)