package events.inbound

import kotlinx.serialization.*

@Serializable
data class InboundUserJoinedRoom(val room: String, val username: String)