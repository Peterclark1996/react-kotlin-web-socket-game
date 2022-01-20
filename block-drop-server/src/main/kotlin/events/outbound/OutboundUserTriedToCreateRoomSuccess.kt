package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserTriedToCreateRoomSuccess(val room: String, val username: String)