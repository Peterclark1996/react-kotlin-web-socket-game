package events

import kotlinx.serialization.*

@Serializable
data class InboundUserJoinedRoom(val roomId: String, val username: String)