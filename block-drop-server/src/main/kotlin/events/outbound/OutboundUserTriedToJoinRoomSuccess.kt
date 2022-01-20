package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserTriedToJoinRoomSuccess(val room: String, val username: String)