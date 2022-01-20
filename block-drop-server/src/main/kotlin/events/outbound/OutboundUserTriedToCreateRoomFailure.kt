package events.outbound

import kotlinx.serialization.*

@Serializable
data class OutboundUserTriedToCreateRoomFailure(val message: String)