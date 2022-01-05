package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserJoinedRoomResult(val roomId: String, val username: String, val success: Boolean)