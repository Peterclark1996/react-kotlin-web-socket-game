package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserJoinedRoomSuccess(val room: String, val username: String)