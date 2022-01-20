package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserTriedToJoinRoomFailure(val message: String)