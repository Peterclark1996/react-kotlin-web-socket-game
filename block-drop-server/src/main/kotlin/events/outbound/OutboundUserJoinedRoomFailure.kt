package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserJoinedRoomFailure(val message: String)